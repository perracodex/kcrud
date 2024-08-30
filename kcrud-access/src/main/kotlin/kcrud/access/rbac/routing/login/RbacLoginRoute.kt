/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.access.rbac.routing.login

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kcrud.access.rbac.plugin.annotation.RbacAPI
import kcrud.access.rbac.service.RbacAdminPanelManager
import kcrud.access.rbac.view.RbacAdminView
import kcrud.access.rbac.view.RbacLoginView
import kcrud.base.env.SessionContext

/**
 * The route for logging into the RBAC admin panel.
 */
@RbacAPI
internal fun Route.rbacLoginRoute() {

    get("rbac/login") {
        RbacAdminPanelManager.getSessionContext(call = call)?.let {
            // If the actor is not null, redirect to the admin view.
            call.respondRedirect(url = RbacAdminView.RBAC_ADMIN_PATH)
        } ?: run {
            // If the actor is null, clear the session and show the login view.
            call.sessions.clear(name = SessionContext.SESSION_NAME)
            call.respondHtml(status = HttpStatusCode.OK) {
                RbacLoginView.build(html = this)
            }
        }
    }

    authenticate(RbacLoginView.RBAC_LOGIN_PATH) {
        post("rbac/login") {
            call.respondRedirect(url = RbacAdminView.RBAC_ADMIN_PATH)
        }
    }
}

