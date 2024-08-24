/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.access.rbac.routing.login

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kcrud.access.rbac.plugin.annotation.RbacAPI
import kcrud.access.rbac.view.RbacLoginView
import kcrud.base.env.SessionContext

/**
 * The route for logging out of the RBAC admin panel.
 */
@RbacAPI
internal fun Route.rbacLogoutRoute() {
    post("rbac/logout") {
        call.sessions.clear(name = SessionContext.SESSION_NAME)
        call.respondRedirect(url = RbacLoginView.RBAC_LOGIN_PATH)
    }
}
