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
import kcrud.access.rbac.plugin.annotation.RbacAPI
import kcrud.access.rbac.service.RbacAdminPanelManager
import kcrud.access.rbac.view.RbacAdminView
import kcrud.access.rbac.view.RbacLoginView
import kcrud.base.env.SessionContext
import kcrud.base.persistence.utils.toUuidOrNull
import kotlin.uuid.Uuid

/**
 * Handles the POST request for the RBAC admin panel, processing updates to RBAC settings based on user input.
 * This includes adjusting roles, scopes, and permissions according to form submissions. The function ensures
 * that only authorized modifications are applied, with redirects handling any unauthorized access attempts.
 */
@RbacAPI
internal fun Route.rbacAdminUpdateRoute() {
    post("rbac/admin") {
        // Retrieve session context or redirect to the login screen if it's missing.
        val sessionContext: SessionContext = RbacAdminPanelManager.getSessionContext(call = call) ?: run {
            call.sessions.clear(name = SessionContext.SESSION_NAME)
            call.respondRedirect(url = RbacLoginView.RBAC_LOGIN_PATH)
            return@post
        }

        // Receive and process form parameters.
        val parameters: Parameters = call.receiveParameters()
        val currentRoleId: Uuid = parameters[RbacAdminView.ROLE_KEY].toUuidOrNull() ?: run {
            call.respond(HttpStatusCode.BadRequest, "Invalid or missing role ID.")
            return@post
        }

        // Fetch the role-specific scope rules for the current role,
        // and update the rules based on the submitted parameters.
        val updates: Map<String, String> = parameters.entries().associate { it.key to it.value.first() }
        val result: RbacAdminPanelManager.UpdateResult = RbacAdminPanelManager.processAdminUpdate(
            sessionContext = sessionContext,
            roleId = currentRoleId,
            updates = updates
        )

        // Respond based on the result of the update operation.
        when (result) {
            // If the update was successful, render the updated RBAC admin panel.
            is RbacAdminPanelManager.UpdateResult.Success -> {
                call.respondHtml(HttpStatusCode.OK) {
                    RbacAdminView.build(
                        html = this,
                        rbacRoles = result.roles,
                        currentRoleId = result.currentRoleId,
                        isUpdated = true,
                        isViewOnly = result.isViewOnly
                    )
                }
            }

            // If the update was unauthorized, clear the session and redirect to the login screen.
            is RbacAdminPanelManager.UpdateResult.Unauthorized -> {
                call.sessions.clear(name = SessionContext.SESSION_NAME)
                call.respondRedirect(url = RbacLoginView.RBAC_LOGIN_PATH)
            }
        }
    }
}
