/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.access.rbac.api.dashboard

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
import kcrud.base.env.CallContext
import kcrud.base.persistence.utils.toUuidOrNull

/**
 * Retrieves the current [CallContext] and renders the RBAC dashboard based
 * on the actor's permissions and role selections.
 * Redirects to the login screen if the [CallContext] is invalid.
 */
@RbacAPI
internal fun Route.rbacDashboardLoadRoute() {
    /**
     * Opens the RBAC dashboard. Redirects to the login screen if the [CallContext] is invalid.
     * @OpenAPITag RBAC
     */
    get("rbac/dashboard") {
        // Attempt to retrieve the CalLContext for RBAC dashboard access. Redirect to the login screen if null.
        val callContext: CallContext = RbacDashboardManager.getCallContext(call = call)
            ?: return@get call.run {
                call.sessions.clear(name = CallContext.SESSION_NAME)
                call.respondRedirect(url = RbacLoginView.RBAC_LOGIN_PATH)
            }

        // Resolve the RBAC access details for the current CallContext.
        RbacDashboardManager.determineAccessDetails(
            callContext = callContext,
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
