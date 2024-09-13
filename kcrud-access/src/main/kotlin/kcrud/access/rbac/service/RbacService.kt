/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.access.rbac.service

import kcrud.access.actor.model.Actor
import kcrud.access.actor.repository.IActorRepository
import kcrud.access.rbac.model.role.RbacRole
import kcrud.access.rbac.model.role.RbacRoleRequest
import kcrud.access.rbac.model.scope.RbacScopeRuleRequest
import kcrud.access.rbac.repository.role.IRbacRoleRepository
import kcrud.access.rbac.repository.scope.IRbacScopeRuleRepository
import kcrud.base.database.schema.admin.rbac.types.RbacAccessLevel
import kcrud.base.database.schema.admin.rbac.types.RbacScope
import kcrud.base.env.SessionContext
import kcrud.base.env.Tracer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import kotlin.uuid.Uuid

/**
 * Service to handle Role-Based Access Control (RBAC) operations.
 *
 * @property actorRepository The [IActorRepository] to handle Actor operations.
 * @property roleRepository The [IRbacRoleRepository] to handle Role operations.
 * @property scopeRuleRepository The [IRbacScopeRuleRepository] to handle scope rule operations.
 */
internal class RbacService(
    private val actorRepository: IActorRepository,
    private val roleRepository: IRbacRoleRepository,
    private val scopeRuleRepository: IRbacScopeRuleRepository
) {
    private val tracer = Tracer<RbacService>()

    /**
     * Cache holding Actor IDs paired with their respective [RbacRole],
     * allowing to quickly check if an Actor has permission to access a scope,
     * whether is locked, and its Role attributes.
     *
     * This approach is better than holding the full [Actor] tree,
     * as it avoids exposing the Actor's password and other sensitive
     * information while cached.
     *
     * The cache is meant to be updated whenever roles are updated.
     */
    private val cache: ConcurrentHashMap<Uuid, ActorRole> = ConcurrentHashMap()

    /** Data class holding role attributes and the actor's locked status. */
    private data class ActorRole(val isLocked: Boolean, val role: RbacRole)

    /** Lock to ensure thread-safe access and updates to the service cache. */
    private val lock: Mutex = Mutex()

    /**
     * Checks if the cache has been populated. If empty, it refreshes it.
     *
     * @return True if the cache is empty, false if populated.
     */
    private suspend fun isCacheEmpty(): Boolean {
        if (cache.isEmpty()) {
            refreshActors()
        }

        return cache.isEmpty()
    }

    /**
     * Refreshes the permissions cache for the given actor, ensuring the service reflects
     * the latest system permissions.
     *
     * If the actor has no scope rules, for example a superuser, it will be removed from
     * the cache if previously cached.
     *
     * @param actorId The id of the Actor to refresh.
     * @throws IllegalStateException If no actor is found with the provided id.
     */
    suspend fun refreshActor(actorId: Uuid): Unit = withContext(Dispatchers.IO) {
        tracer.info("Refreshing RBAC cache for actor with ID: $actorId")

        val actor: Actor? = actorRepository.findById(actorId = actorId)
        checkNotNull(actor) { "No actor found with id $actorId." }

        // Filter out Actors without any scope rules. Maybe a superuser.
        if (actor.role.scopeRules.isEmpty()) {
            if (cache.containsKey(actorId)) {
                tracer.warning("Removing actor with ID: $actorId from RBAC cache.")
                lock.withLock {
                    cache.remove(actorId)
                }
            } else {
                tracer.warning("Actor with ID: $actorId not found in RBAC cache.")
            }

            return@withContext
        }

        // Set the new ActorRole entry in the cache.
        val actorRole = ActorRole(isLocked = actor.isLocked, role = actor.role)
        lock.withLock {
            cache[actorId] = actorRole
        }

        tracer.info("RBAC cache refreshed for actor with ID: $actorId.")
    }

    /**
     * Refreshes the permissions cache for all actors, ensuring the service reflects
     * the latest system permissions.
     *
     * Actors that no longer have any scope rules will be removed from the cache.
     */
    suspend fun refreshActors(): Unit = withContext(Dispatchers.IO) {
        tracer.info("Refreshing RBAC cache for all actors.")

        var totalActors: Int

        // Retrieve all actors from the database, filtering out those without any scope rules.
        val newCache: ConcurrentHashMap<Uuid, ActorRole> = actorRepository.findAll().also { actors ->
            totalActors = actors.size
        }.filter { actor ->
            actor.role.scopeRules.isNotEmpty()
        }.associateTo(ConcurrentHashMap()) { actor ->
            actor.id to ActorRole(isLocked = actor.isLocked, role = actor.role)
        }.also { actors ->
            if (actors.isEmpty()) {
                tracer.warning("No actors found with scope rules. RBAC cache is empty.")
            }
        }

        // Replace the current cache with the new updated one.
        // The cache can be empty if no actors have scope rules.
        lock.withLock {
            cache.clear()
            cache.putAll(newCache)
        }

        tracer.info("RBAC cache refreshed. ${newCache.size} out of $totalActors actors have scope rules.")
    }

    /**
     * Checks if the given [SessionContext] has the given [RbacAccessLevel] for the given [RbacScope].
     *
     * @param sessionContext The current request [SessionContext].
     * @param scope The [RbacScope] to check.
     * @param accessLevel The [RbacAccessLevel] to check. Expected to have minimal this access level.
     * @return True if the [SessionContext] has permission, false otherwise.
     */
    suspend fun hasPermission(
        sessionContext: SessionContext,
        scope: RbacScope,
        accessLevel: RbacAccessLevel
    ): Boolean {
        if (isCacheEmpty())
            return false

        return cache[sessionContext.actorId]?.let { actorRole ->
            !actorRole.isLocked && actorRole.role.scopeRules.any { scopeRule ->
                (scopeRule.roleId == sessionContext.roleId) &&
                        (scopeRule.scope == scope) &&
                        scopeRule.accessLevel.hasSufficientPrivileges(requiredAccessLevel = accessLevel)
            }
        } ?: false
    }

    /**
     * Retrieves the [RbacAccessLevel] for the given [SessionContext] and [RbacScope].
     *
     * @param sessionContext The [SessionContext] to check.
     * @param scope The [RbacScope] to check.
     * @return The [RbacAccessLevel] for the given [SessionContext] and [RbacScope].
     */
    suspend fun getPermissionLevel(sessionContext: SessionContext, scope: RbacScope): RbacAccessLevel {
        if (isCacheEmpty()) {
            return RbacAccessLevel.NONE
        }

        return cache[sessionContext.actorId]?.let { role ->
            if (role.isLocked) {
                RbacAccessLevel.NONE
            } else {
                role.role.scopeRules.find { scopeRule ->
                    scopeRule.roleId == sessionContext.roleId && scopeRule.scope == scope
                }?.accessLevel ?: RbacAccessLevel.NONE
            }
        } ?: RbacAccessLevel.NONE
    }

    /**
     * Retrieves all the [RbacRole] entries.
     *
     * @return List with all the existing [RbacRole] entries.
     */
    suspend fun findAllRoles(): List<RbacRole> = withContext(Dispatchers.IO) {
        return@withContext roleRepository.findAll()
    }

    /**
     * Retrieves the [RbacRole] for the given [roleId].
     *
     * @param roleId The id of the role to retrieve.
     * @return The [RbacRole] for the given [roleId], or null if it doesn't exist.
     */
    suspend fun findRoleById(roleId: Uuid): RbacRole? = withContext(Dispatchers.IO) {
        return@withContext roleRepository.findById(roleId = roleId)
    }

    /**
     * Retrieves the [RbacRole] for the given [actorId].
     *
     * @param actorId The id of the actor to which the role is associated.
     * @return The [RbacRole] for the given [actorId], or null if it doesn't exist.
     */
    suspend fun findRoleByActorId(actorId: Uuid): RbacRole? = withContext(Dispatchers.IO) {
        return@withContext roleRepository.findByActorId(actorId = actorId)
    }

    /**
     * Creates a new role.
     *
     * @param roleRequest The new [RbacRoleRequest] to create.
     * @return The created [RbacRole].
     */
    suspend fun createRole(
        roleRequest: RbacRoleRequest
    ): RbacRole = withContext(Dispatchers.IO) {
        tracer.info("Creating new role: ${roleRequest.roleName}")

        val roleId: Uuid = roleRepository.create(roleRequest = roleRequest)

        // After creating the role must refresh the cache to reflect the new role.
        refreshActors()

        return@withContext roleRepository.findById(roleId = roleId)!!
    }

    /**
     * Updates an existing role for the given [roleId].
     * The current existing roles will be replaced by the new ones.
     *
     * @param roleId The id of the role to update.
     * @param roleRequest The [RbacRoleRequest] to update the role from.
     * @return The updated [RbacRole] if the update was successful, null otherwise.
     */
    suspend fun updateRole(
        roleId: Uuid,
        roleRequest: RbacRoleRequest
    ): RbacRole? = withContext(Dispatchers.IO) {
        tracer.info("Updating role with ID: $roleId")

        val updateCount: Int = roleRepository.update(
            roleId = roleId,
            roleRequest = roleRequest
        )

        // After updating the role must refresh the cache to reflect the changes.
        refreshActors()

        return@withContext if (updateCount > 0) {
            roleRepository.findById(roleId = roleId)
        } else {
            null
        }
    }

    /**
     * Updates an existing role with the given set of [RbacScopeRuleRequest] entries.
     *
     * All the existing scope rules for the given [roleId] will be replaced by the new ones.
     *
     * @param roleId The id of the role for which the rules are updated.
     * @param scopeRuleRequests The new set of [RbacScopeRuleRequest] entries to set.
     * @return The number of rows updated.
     */
    suspend fun updateScopeRules(
        roleId: Uuid,
        scopeRuleRequests: List<RbacScopeRuleRequest>
    ): Int = withContext(Dispatchers.IO) {
        tracer.info("Updating scope rules for role with ID: $roleId")

        val updateCount: Int = scopeRuleRepository.replace(
            roleId = roleId,
            scopeRuleRequests = scopeRuleRequests
        )

        // After updating the scope rules must refresh the cache to reflect the changes.
        refreshActors()

        return@withContext updateCount
    }
}
