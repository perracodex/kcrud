/*
 * Copyright (c) 2023-Present Perraco. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.access.rbac.service

import kcrud.access.actor.entity.ActorEntity
import kcrud.access.actor.repository.IActorRepository
import kcrud.access.rbac.entity.resource_rule.RbacResourceRuleEntity
import kcrud.access.rbac.entity.resource_rule.RbacResourceRuleRequest
import kcrud.access.rbac.entity.role.RbacRoleEntity
import kcrud.access.rbac.entity.role.RbacRoleRequest
import kcrud.access.rbac.repository.resource_rule.IRbacResourceRuleRepository
import kcrud.access.rbac.repository.role.IRbacRoleRepository
import kcrud.base.database.schema.admin.rbac.types.RbacAccessLevel
import kcrud.base.database.schema.admin.rbac.types.RbacResource
import kcrud.base.env.SessionContext
import kcrud.base.env.Tracer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Service to handle Role-Based Access Control (RBAC) operations.
 *
 * @param actorRepository The [IActorRepository] to handle Actor operations.
 * @param roleRepository The [IRbacRoleRepository] to handle Role operations.
 * @param resourceRuleRepository The [IRbacResourceRuleRepository] to handle resource rule operations.
 */
class RbacService(
    private val actorRepository: IActorRepository,
    private val roleRepository: IRbacRoleRepository,
    private val resourceRuleRepository: IRbacResourceRuleRepository
) {
    private val tracer = Tracer<RbacService>()

    /**
     * Cache holding Actor IDs paired with their respective [RbacRoleEntity],
     * allowing to quickly check if an Actor has permission to access a resource,
     * whether is locked, and its Role attributes.
     *
     * This approach is better than holding the full [ActorEntity] tree,
     * as it avoids exposing the Actor's password and other sensitive
     * information while cached.
     *
     * The cache is meant to be updated whenever roles are updated.
     */
    private val cache: ConcurrentHashMap<UUID, ActorRole> = ConcurrentHashMap()

    /**
     * Data class holding role attributes and the actor's locked status.
     */
    private data class ActorRole(val isLocked: Boolean, val role: RbacRoleEntity)

    /**
     * Lock to ensure thread-safe access and updates to the service cache.
     */
    private val lock: Mutex = Mutex()

    /**
     * Checks if the cache has been populated. If empty, it refreshes it.
     *
     * @return True if the cache is empty, false if populated.
     */
    private suspend fun isCacheEmpty(): Boolean {
        if (cache.isEmpty()) {
            refresh()
        }

        return cache.isEmpty()
    }

    /**
     * Refreshes the cache of [RbacResourceRuleEntity] entries, ensuring the service reflects
     * the latest permissions. If an [actorId] is provided, only the permissions for that actor are refreshed;
     * otherwise permissions for all actors are refreshed.
     *
     * @param actorId The id of the Actor to refresh. If null, refreshes permissions for all Actors.
     */
    suspend fun refresh(actorId: UUID? = null) = withContext(Dispatchers.IO) {
        tracer.info("Refreshing RBAC cache.")

        var targetActors: List<ActorEntity> = if (actorId == null) {
            actorRepository.findAll()
        } else {
            actorRepository.findById(actorId = actorId)?.let { listOf(it) } ?: emptyList()
        }

        // Filter out Actors without any resource rules.
        targetActors = targetActors.filter { actor ->
            actor.role.resourceRules.isNotEmpty()
        }

        if (actorId != null && targetActors.isNotEmpty()) {
            // When refreshing a single actor, update only their cache entry.
            val actor: ActorEntity = targetActors.first() // There should only be one actor in this case.
            val actorRole = ActorRole(isLocked = actor.isLocked, role = actor.role)

            lock.withLock {
                cache[actorId] = actorRole
            }
        } else {
            // Prepare the new cache mapping for all actors.
            val newCache: ConcurrentHashMap<UUID, ActorRole> = ConcurrentHashMap()
            newCache.putAll(
                targetActors.associateBy(
                    { actor -> actor.id },
                    { actor -> ActorRole(isLocked = actor.isLocked, role = actor.role) }
                )
            )

            // Replace the current cache with the new one.
            lock.withLock {
                cache.clear()
                cache.putAll(newCache)
            }
        }

        tracer.info("RBAC cache refreshed.")
    }

    /**
     * Checks if the given [SessionContext] has the given [RbacAccessLevel] for the given [RbacResource].
     *
     * @param sessionContext The current request [SessionContext].
     * @param accessLevel The [RbacAccessLevel] to check.
     * @param resource The [RbacResource] to check.
     * @return True if the [SessionContext] has permission, false otherwise.
     */
    suspend fun hasPermission(
        sessionContext: SessionContext,
        resource: RbacResource,
        accessLevel: RbacAccessLevel
    ): Boolean {
        if (isCacheEmpty())
            return false

        return cache[sessionContext.actorId]?.let { actorRole ->
            !actorRole.isLocked && actorRole.role.resourceRules.any { resourceRule ->
                (resourceRule.roleId == sessionContext.roleId) &&
                        (resourceRule.resource == resource) &&
                        resourceRule.accessLevel.hasSufficientPrivileges(requiredAccessLevel = accessLevel)
            }
        } ?: false
    }

    /**
     * Retrieves the [RbacAccessLevel] for the given [SessionContext] and [RbacResource].
     *
     * @param sessionContext The [SessionContext] to check.
     * @param resource The [RbacResource] to check.
     * @return The [RbacAccessLevel] for the given [SessionContext] and [RbacResource].
     */
    suspend fun getPermissionLevel(sessionContext: SessionContext, resource: RbacResource): RbacAccessLevel {
        if (isCacheEmpty())
            RbacAccessLevel.NONE

        return cache[sessionContext.actorId]?.let { role ->
            if (role.isLocked) {
                RbacAccessLevel.NONE
            } else {
                role.role.resourceRules.find { resourceRule ->
                    resourceRule.roleId == sessionContext.roleId && resourceRule.resource == resource
                }?.accessLevel ?: RbacAccessLevel.NONE
            }
        } ?: RbacAccessLevel.NONE
    }

    /**
     * Retrieves all the [RbacRoleEntity] entries.
     *
     * @return List with all the existing [RbacRoleEntity] entries.
     */
    suspend fun findAllRoles(): List<RbacRoleEntity> = withContext(Dispatchers.IO) {
        roleRepository.findAll()
    }

    /**
     * Retrieves the [RbacRoleEntity] for the given [roleId].
     *
     * @param roleId The id of the role to retrieve.
     * @return The [RbacRoleEntity] for the given [roleId], or null if it doesn't exist.
     */
    suspend fun findRoleById(roleId: UUID): RbacRoleEntity? = withContext(Dispatchers.IO) {
        roleRepository.findById(roleId = roleId)
    }

    /**
     * Retrieves the [RbacRoleEntity] for the given [actorId].
     *
     * @param actorId The id of the actor to which the role is associated.
     * @return The [RbacRoleEntity] for the given [actorId], or null if it doesn't exist.
     */
    suspend fun findRoleByActorId(actorId: UUID): RbacRoleEntity? = withContext(Dispatchers.IO) {
        roleRepository.findByActorId(actorId = actorId)
    }

    /**
     * Creates a new role.
     *
     * @param roleRequest The new [RbacRoleRequest] to create.
     * @return The created [RbacRoleEntity].
     */
    suspend fun createRole(
        roleRequest: RbacRoleRequest
    ): RbacRoleEntity = withContext(Dispatchers.IO) {
        tracer.info("Creating new role: ${roleRequest.roleName}")

        val roleId: UUID = roleRepository.create(roleRequest = roleRequest)

        refresh()

        roleRepository.findById(roleId = roleId)!!
    }

    /**
     * Updates an existing role for the given [roleId].
     * The current existing roles will be replaced by the new ones.
     *
     * @param roleId The id of the role to update.
     * @param roleRequest The [RbacRoleRequest] to update the role from.
     * @return The updated [RbacRoleEntity] if the update was successful, null otherwise.
     */
    suspend fun updateRole(
        roleId: UUID,
        roleRequest: RbacRoleRequest
    ): RbacRoleEntity? = withContext(Dispatchers.IO) {
        tracer.info("Updating role with ID: $roleId")

        val updateCount: Int = roleRepository.update(
            roleId = roleId,
            roleRequest = roleRequest
        )

        refresh()

        if (updateCount > 0) {
            roleRepository.findById(roleId = roleId)
        } else {
            null
        }
    }

    /**
     * Updates an existing role with the given set of [RbacResourceRuleRequest] entries.
     *
     * All the existing resource rules for the given [roleId] will be replaced by the new ones.
     *
     * @param roleId The id of the role for which the rules are updated.
     * @param resourceRuleRequests The new set of [RbacResourceRuleRequest] entries to set.
     * @return The number of rows updated.
     */
    suspend fun updateResourceRules(
        roleId: UUID,
        resourceRuleRequests: List<RbacResourceRuleRequest>
    ): Int = withContext(Dispatchers.IO) {
        tracer.info("Updating resource rules for role with ID: $roleId")

        val updateCount: Int = resourceRuleRepository.replace(
            roleId = roleId,
            resourceRuleRequests = resourceRuleRequests
        )

        refresh()

        updateCount
    }
}
