/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.access.domain.rbac.api.login

import io.github.perracodex.kopapi.dsl.operation.api
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kcrud.access.domain.rbac.plugin.annotation.RbacApi
import kcrud.access.domain.rbac.view.RbacLoginView
import kcrud.core.context.clearContext

/**
 * Manages the session termination and redirection to the login page.
 * Clears the current session and redirects the actor to ensure a clean logout process.
 */
@RbacApi
internal fun Route.rbacLogoutRoute() {
    post("/rbac/logout") {
        call.clearContext()
        call.respondRedirect(url = RbacLoginView.RBAC_LOGIN_PATH)
    } api {
        tags = setOf("RBAC")
        summary = "Logout from the RBAC dashboard."
        description = "Logout from the RBAC dashboard and redirect to the login page."
        operationId = "rbacLogout"
        response<String>(status = HttpStatusCode.Found) {
            description = "Redirect to the RBAC login page."
        }
    }
}
