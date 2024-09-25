/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.access.rbac.api.dashboard

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kcrud.access.rbac.plugin.annotation.RbacAPI
import kcrud.access.rbac.service.RbacDashboardManager
import kcrud.access.rbac.view.RbacDashboardView
import kcrud.access.rbac.view.RbacLoginView
import kcrud.core.context.SessionContext
import kcrud.core.context.clearContext
import kcrud.core.context.getContext
import kcrud.core.persistence.utils.toUuidOrNull

/**
 * Retrieves the current [SessionContext] and renders the RBAC dashboard based
 * on the actor's permissions and role selections.
 * Redirects to the login screen if the [SessionContext] is invalid.
 */
@RbacAPI
internal fun Route.rbacDashboardLoadRoute() {
    /**
     * Opens the RBAC dashboard. Redirects to the login screen if the [SessionContext] is invalid.
     * @OpenAPITag RBAC
     */
    get("rbac/dashboard") {
        // Attempt to retrieve the SessionContext for RBAC dashboard access. Redirect to the login screen if null.
        val sessionContext: SessionContext = call.getContext()
        if (!RbacDashboardManager.hasPermission(sessionContext = sessionContext)) {
            call.clearContext()
            call.respondRedirect(url = RbacLoginView.RBAC_LOGIN_PATH)
            return@get
        }

        // Resolve the RBAC access details for the current SessionContext.
        RbacDashboardManager.determineAccessDetails(
            sessionContext = sessionContext,
            roleId = call.parameters[RbacDashboardView.ROLE_KEY].toUuidOrNull()
        ).let { dashboardContext ->
            // Respond with HTML view of the RBAC dashboard.
            call.respondHtml(status = HttpStatusCode.OK) {
                RbacDashboardView.build(
                    html = this,
                    isUpdated = false,
                    dashboardContext = dashboardContext
                )
            }
        }
    }
}
