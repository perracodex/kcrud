/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.access.rbac.routing.admin

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kcrud.access.rbac.entity.role.RbacRoleEntity
import kcrud.access.rbac.entity.scope.RbacScopeRuleRequest
import kcrud.access.rbac.plugin.annotation.RbacAPI
import kcrud.access.rbac.service.RbacService
import kcrud.access.rbac.view.RbacAdminView
import kcrud.access.rbac.view.RbacLoginView
import kcrud.base.database.schema.admin.rbac.types.RbacAccessLevel
import kcrud.base.database.schema.admin.rbac.types.RbacScope
import kcrud.base.env.SessionContext
import kcrud.base.persistence.utils.toUuidOrNull
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.uuid.Uuid

/**
 * Handles the POST request for the RBAC admin panel.
 */
@RbacAPI
internal fun Route.rbacSAdminRoutePost(rbacService: RbacService) {
    post("rbac/admin") {
        val sessionContext: SessionContext? = call.sessions.get<SessionContext>()
        sessionContext ?: run {
            call.respondRedirect(url = RbacLoginView.RBAC_LOGIN_PATH)
            return@post
        }

        val parameters: Parameters = call.receiveParameters()
        val currentRoleId: Uuid? = parameters[RbacAdminView.ROLE_KEY].toUuidOrNull()

        currentRoleId?.let { roleId ->
            // Set the new scope rules for the role.
            RbacScopeRulesManager.process(roleId = roleId, parameters = parameters)

            // If no longer has access to the RBAC admin panel, redirect to the login.
            val permissionLevel: RbacAccessLevel = rbacService.getPermissionLevel(
                sessionContext = sessionContext,
                scope = RbacScope.RBAC_ADMIN
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
                scope = RbacScope.RBAC_ADMIN
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
private object RbacScopeRulesManager : KoinComponent {
    suspend fun process(roleId: Uuid, parameters: Parameters) {
        val scopeRulesRequests: MutableList<RbacScopeRuleRequest> = mutableListOf()

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
                    val scopeRuleRequest = RbacScopeRuleRequest(
                        scope = RbacScope.valueOf(accessKey.scope),
                        accessLevel = accessLevel
                    )
                    scopeRulesRequests.add(scopeRuleRequest)
                }
            }

        val rbacService: RbacService by inject()
        rbacService.updateScopeRules(
            roleId = roleId,
            scopeRuleRequests = scopeRulesRequests.toList()
        )
    }
}
