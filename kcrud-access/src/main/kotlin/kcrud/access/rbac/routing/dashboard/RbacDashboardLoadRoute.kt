/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.access.rbac.routing.dashboard

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kcrud.access.rbac.plugin.annotation.RbacAPI
import kcrud.access.rbac.service.RbacDashboardManager
import kcrud.access.rbac.view.RbacDashboardView
import kcrud.access.rbac.view.RbacLoginView
import kcrud.base.env.SessionContext
import kcrud.base.persistence.utils.toUuidOrNull

/**
 * Retrieves the current session context and renders the RBAC dashboard based
 * on the actor's permissions and role selections.
 * Redirects to the login screen if the session context is invalid.
 */
@RbacAPI
internal fun Route.rbacDashboardLoadRoute() {
    get("rbac/dashboard") {
        // Attempt to retrieve the session context for RBAC dashboard access. Redirect to the login screen if null.
        val sessionContext: SessionContext = RbacDashboardManager.getSessionContext(call = call)
            ?: return@get call.run {
                call.sessions.clear(name = SessionContext.SESSION_NAME)
                call.respondRedirect(url = RbacLoginView.RBAC_LOGIN_PATH)
            }

        // Resolve the RBAC access details for the current session context.
        RbacDashboardManager.determineAccessDetails(
            sessionContext = sessionContext,
            roleId = call.parameters[RbacDashboardView.ROLE_KEY].toUuidOrNull()
        ).let { context ->
            // Respond with HTML view of the RBAC dashboard.
            call.respondHtml(status = HttpStatusCode.OK) {
                RbacDashboardView.build(
                    html = this,
                    isUpdated = false,
                    dashboardContext = context
                )
            }
        }
    }
}
