/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.access.rbac.api.login

import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kcrud.access.rbac.plugin.annotation.RbacApi
import kcrud.access.rbac.view.RbacDashboardView
import kcrud.access.rbac.view.RbacLoginView

/**
 * Manages the login submission process. Authenticates the provided credentials and,
 * upon successful authentication, redirects the actor to the dashboard.
 * Unsuccessful attempts are handled by the authentication setup.
 */
@RbacApi
internal fun Route.rbacLoginSubmissionRoute() {
    authenticate(RbacLoginView.RBAC_LOGIN_PATH) {
        /**
         * Redirects actors to the dashboard after successful authentication.
         * @OpenAPITag RBAC
         */
        post("rbac/login") {
            call.respondRedirect(url = RbacDashboardView.RBAC_DASHBOARD_PATH)
        }
    }
}
