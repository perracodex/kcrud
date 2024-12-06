/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.access.domain.actor.service

import kcrud.access.credential.CredentialService
import kcrud.access.domain.actor.model.ActorRequest
import kcrud.access.domain.rbac.model.role.RbacRole
import kcrud.access.domain.rbac.model.role.RbacRoleRequest
import kcrud.access.domain.rbac.model.scope.RbacScopeRuleRequest
import kcrud.access.domain.rbac.service.RbacService
import kcrud.core.env.Tracer
import kcrud.database.schema.admin.rbac.type.RbacAccessLevel
import kcrud.database.schema.admin.rbac.type.RbacScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Singleton object to create default Actors with their respective roles.
 *
 * These are created only if no Actors at all are found in the database.
 */
internal object DefaultActorFactory : KoinComponent {
    private val tracer: Tracer = Tracer<DefaultActorFactory>()

    /**
     * The default role names.
     * Ideally, these should be defined in a database table, instead of an enum.
     * These are used to create default Actors and their respective roles,
     * when none are found in the database.
     */
    enum class RoleName {
        /** ADMIN actor role. Usually with full access. */
        ADMIN,

        /** GUEST actor role. Usually with very limited access. */
        GUEST
    }

    /**
     * Refresh the Credentials and RBAC services on application start,
     * so the caches are up-to-date and ready to handle requests.
     */
    suspend fun refresh(): Unit = withContext(Dispatchers.IO) {
        tracer.info("Refreshing actors.")

        // Ensure the database has any Actors, if none exist then create the default ones.
        createIfMissing()

        coroutineScope {
            launch {
                val credentialService: CredentialService by inject()
                credentialService.refreshActors()
            }

            launch {
                val rbacService: RbacService by inject()
                rbacService.refreshActors()
            }
        }
    }

    /**
     * Creates default Actors with their respective roles if none are found in the database.
     */
    private suspend fun createIfMissing() {
        val actorService: ActorService by inject()
        if (actorService.actorsExist()) {
            return
        }

        tracer.info("No actors found. Creating default ones.")
        val rbacRoles: List<RbacRole> = getRoles()

        rbacRoles.forEach { role ->
            val credential: String = role.roleName.lowercase()

            actorService.create(
                actorRequest = ActorRequest(
                    roleId = role.id,
                    username = credential,
                    password = credential,
                    isLocked = false
                )
            )
        }
    }

    /**
     * Gets the default roles, creating them if none are found in the database.
     */
    private suspend fun getRoles(): List<RbacRole> {
        val rbacService: RbacService by inject()
        val rbacRoles: List<RbacRole> = rbacService.findAllRoles()
        if (rbacRoles.isNotEmpty()) {
            return rbacRoles
        }

        // Create default roles if none are found.
        RoleName.entries.forEach { role ->
            val adminScopeRules: List<RbacScopeRuleRequest> = if (role == RoleName.ADMIN) {
                RbacScope.entries.map { scope ->
                    RbacScopeRuleRequest(
                        scope = scope,
                        accessLevel = RbacAccessLevel.FULL,
                        fieldRules = null
                    )
                }
            } else {
                emptyList()
            }

            val roleRequest = RbacRoleRequest(
                roleName = role.name,
                description = null,
                isSuper = (role == RoleName.ADMIN),
                scopeRules = adminScopeRules
            )

            rbacService.createRole(roleRequest = roleRequest)
        }

        return rbacService.findAllRoles()
    }
}
