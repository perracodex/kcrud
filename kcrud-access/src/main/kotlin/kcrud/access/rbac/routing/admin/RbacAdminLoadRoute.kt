/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.access.rbac.routing.admin

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kcrud.access.rbac.plugin.annotation.RbacAPI
import kcrud.access.rbac.service.RbacAdminPanelManager
import kcrud.access.rbac.view.RbacAdminView
import kcrud.access.rbac.view.RbacLoginView
import kcrud.base.env.SessionContext
import kcrud.base.persistence.utils.toUuidOrNull

/**
 * Handles the GET request for the RBAC admin panel. This function retrieves the current session context,
 * validates user access, and renders the admin panel based on the user's RBAC permissions and role selections.
 * It ensures secure and role-appropriate visibility of RBAC configurations.
 */
@RbacAPI
internal fun Route.rbacAdminLoadRoute() {
    get("rbac/admin") {
        // Attempt to retrieve the session context for RBAC admin access. Redirect to the login screen if null.
        val sessionContext: SessionContext = RbacAdminPanelManager.getSessionContext(call = call) ?: run {
            call.sessions.clear(name = SessionContext.SESSION_NAME)
            call.respondRedirect(url = RbacLoginView.RBAC_LOGIN_PATH)
            return@get
        }

        // Resolve the RBAC access details for the current session context.
        val accessDetails: RbacAdminPanelManager.AccessDetails = RbacAdminPanelManager.determineAccessDetails(
            sessionContext = sessionContext,
            roleId = call.parameters[RbacAdminView.ROLE_KEY].toUuidOrNull()
        )

        // Respond with HTML view of the RBAC admin panel.
        call.respondHtml(status = HttpStatusCode.OK) {
            RbacAdminView.build(
                html = this,
                rbacRoles = accessDetails.rbacRoles,
                currentRoleId = accessDetails.currentRole.id,
                isUpdated = false,
                isViewOnly = accessDetails.isViewOnly
            )
        }
    }
}
