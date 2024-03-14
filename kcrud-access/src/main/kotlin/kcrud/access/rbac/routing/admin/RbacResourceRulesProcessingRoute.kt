/*
 * Copyright (c) 2023-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.access.rbac.routing.admin

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kcrud.access.rbac.entities.resource_rule.RbacResourceRuleRequest
import kcrud.access.rbac.entities.role.RbacRoleEntity
import kcrud.access.rbac.plugin.annotation.RbacAPI
import kcrud.access.rbac.service.RbacService
import kcrud.access.rbac.views.RbacAdminView
import kcrud.access.rbac.views.RbacLoginView
import kcrud.access.system.SessionContext
import kcrud.base.database.schema.admin.rbac.types.RbacAccessLevel
import kcrud.base.database.schema.admin.rbac.types.RbacResource
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*

@RbacAPI
fun Route.rbacResourceRulesProcessingRoute(rbacService: RbacService) {
    post("rbac/admin") {
        val sessionContext: SessionContext? = call.sessions.get<SessionContext>()
        if (sessionContext == null) {
            call.respondRedirect(url = RbacLoginView.RBAC_LOGIN_PATH)
            return@post
        }

        val parameters: Parameters = call.receiveParameters()
        val currentRoleId: UUID? = parameters[RbacAdminView.ROLE_KEY]?.let(UUID::fromString)

        currentRoleId?.let { roleId ->
            // Set the new resource rules for the role.
            RbacResourceRulesManager.process(roleId = roleId, parameters = parameters)

            // If no longer has access to the RBAC admin panel, redirect to the login.
            val permissionLevel: RbacAccessLevel = rbacService.getPermissionLevel(
                sessionContext = sessionContext,
                resource = RbacResource.RBAC_ADMIN
            )
            if (permissionLevel == RbacAccessLevel.NONE) {
                call.sessions.clear(name = SessionContext.SESSION_NAME)
                call.respondRedirect(url = RbacLoginView.RBAC_LOGIN_PATH)
                return@post
            }

            // Reload the roles after update for immediate feedback.
            val rbacRoles: List<RbacRoleEntity> = rbacService.findAllRoles()
            val rbacAccessLevel: RbacAccessLevel = rbacService.getPermissionLevel(
                sessionContext = sessionContext,
                resource = RbacResource.RBAC_ADMIN
            )

            call.respondHtml(status = HttpStatusCode.OK) {
                val isViewOnly: Boolean = (rbacAccessLevel == RbacAccessLevel.VIEW)

                RbacAdminView.build(
                    html = this,
                    rbacRoles = rbacRoles,
                    currentRoleId = roleId,
                    isUpdated = true,
                    isViewOnly = isViewOnly
                )
            }
        }
    }
}

/**
 * Process the parameters received from the RBAC admin page.
 */
@RbacAPI
private object RbacResourceRulesManager : KoinComponent {
    suspend fun process(roleId: UUID, parameters: Parameters) {
        val resourceRulesRequests: MutableList<RbacResourceRuleRequest> = mutableListOf()

        parameters.entries()
            .filter { it.key.startsWith(prefix = RbacAdminView.ROLE_ITEM_KEY) }
            .map {
                it to Json.decodeFromString<RbacAdminView.AccessLevelKeyData>(
                    it.key.removePrefix(prefix = RbacAdminView.ROLE_ITEM_KEY)
                )
            }
            .filter { (_, accessKey) -> !accessKey.isLocked }
            .forEach { (paramEntry, accessKey) ->
                val accessLevel: RbacAccessLevel = RbacAccessLevel.valueOf(paramEntry.value.first())

                if (accessLevel != RbacAccessLevel.NONE) {
                    val resourceRuleRequest = RbacResourceRuleRequest(
                        resource = RbacResource.valueOf(accessKey.resource),
                        accessLevel = accessLevel
                    )
                    resourceRulesRequests.add(resourceRuleRequest)
                }
            }

        val rbacService: RbacService by inject()
        rbacService.updateResourceRules(
            roleId = roleId,
            resourceRuleRequests = resourceRulesRequests.toList()
        )
    }
}
