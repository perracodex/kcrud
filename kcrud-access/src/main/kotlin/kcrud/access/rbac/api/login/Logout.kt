/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.access.rbac.api.login

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kcrud.access.rbac.plugin.annotation.RbacAPI
import kcrud.access.rbac.view.RbacLoginView
import kcrud.base.env.CallContext

/**
 * Manages the session termination and redirection to the login page.
 * Clears the current session and redirects the actor to ensure a clean logout process.
 */
@RbacAPI
internal fun Route.rbacLogoutRoute() {
    /**
     * Clears the session and redirects to the login page.
     * @OpenAPITag RBAC
     */
    post("rbac/logout") {
        call.sessions.clear(name = CallContext.SESSION_NAME)
        call.respondRedirect(url = RbacLoginView.RBAC_LOGIN_PATH)
    }
}
