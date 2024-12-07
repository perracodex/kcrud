/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.access.domain.rbac.api.login

import io.github.perracodex.kopapi.dsl.operation.api
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kcrud.access.domain.rbac.annotation.RbacApi
import kcrud.access.domain.rbac.view.RbacDashboardView
import kcrud.access.domain.rbac.view.RbacLoginView

/**
 * Manages the login submission process. Authenticates the provided credentials and,
 * upon successful authentication, redirects the actor to the dashboard.
 * Unsuccessful attempts are handled by the authentication setup.
 */
@RbacApi
internal fun Route.rbacLoginSubmissionRoute() {
    authenticate(RbacLoginView.RBAC_LOGIN_PATH) {
        post("/rbac/login") {
            call.respondRedirect(url = RbacDashboardView.RBAC_DASHBOARD_PATH)
        } api {
            tags = setOf("RBAC")
            summary = "Submit the RBAC login form."
            description = "Submit the RBAC login form to authenticate and login and redirect to the dashboard."
            operationId = "rbacLoginSubmission"
            response<String>(status = HttpStatusCode.Found) {
                description = "Redirect to the RBAC dashboard."
            }
        }
    }
}
