/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.access.actor.service

import kcrud.access.actor.entities.ActorRequest
import kcrud.access.credential.CredentialService
import kcrud.access.rbac.entities.resource_rule.RbacResourceRuleRequest
import kcrud.access.rbac.entities.role.RbacRoleEntity
import kcrud.access.rbac.entities.role.RbacRoleRequest
import kcrud.access.rbac.service.RbacService
import kcrud.base.database.schema.admin.rbac.types.RbacAccessLevel
import kcrud.base.database.schema.admin.rbac.types.RbacResource
import kcrud.base.infrastructure.utils.Tracer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Singleton object to create default Actors with their respective roles.
 *
 * These are created only if no Actors at all are found in the database.
 */
object DefaultActorFactory : KoinComponent {
    private val tracer = Tracer<DefaultActorFactory>()

    /**
     * The default role names.
     * Ideally, these should be defined in a database table, instead of an enum.
     * These are used to create default Actors and their respective roles,
     * when none are found in the database.
     */
    enum class RoleName {
        ADMIN,
        GUEST
    }

    /**
     * Refresh the Credentials and RBAC services on application start,
     * so the caches are up-to-date and ready to handle requests.
     */
    fun refresh() {
        CoroutineScope(Dispatchers.IO).launch {
            // Ensure the database has any Actors, if none exist then create the default ones.
            verify()

            launch {
                val credentialService: CredentialService by inject()
                credentialService.refresh()
            }

            launch {
                val rbacService: RbacService by inject()
                rbacService.refresh()
            }
        }
    }

    /**
     * Creates default Actors with their respective roles
     * if none are found in the database.
     */
    private suspend fun verify() {
        val actorService: ActorService by inject()

        if (!actorService.actorsExist()) {
            tracer.info("No actors found. Creating default ones.")
            createActors(actorService = actorService)
        }
    }

    private suspend fun createActors(actorService: ActorService) {
        val rbacService: RbacService by inject()
        var rbacRoles: List<RbacRoleEntity> = rbacService.findAllRoles()

        // Create roles if none are found.
        if (rbacRoles.isEmpty()) {
            rbacRoles = createRoles(rbacService = rbacService)
        }

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

    private suspend fun createRoles(rbacService: RbacService): List<RbacRoleEntity> {
        RoleName.entries.forEach { role ->
            val adminResourceRules: List<RbacResourceRuleRequest> = if (role == RoleName.ADMIN) {
                RbacResource.entries.map { resource ->
                    RbacResourceRuleRequest(
                        resource = resource,
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
                resourceRules = adminResourceRules
            )

            rbacService.createRole(roleRequest = roleRequest)
        }

        return rbacService.findAllRoles()
    }
}
