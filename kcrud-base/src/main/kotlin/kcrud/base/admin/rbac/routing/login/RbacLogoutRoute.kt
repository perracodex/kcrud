/*
 * Copyright (c) 2023-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.admin.rbac.routing.login

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kcrud.base.admin.rbac.plugin.annotation.RbacAPI
import kcrud.base.admin.rbac.views.RbacLoginView
import kcrud.base.infrastructure.env.SessionContext

@RbacAPI
fun Route.rbacLogoutRoute() {
    route("/rbac/logout") {
        post {
            call.sessions.clear(name = SessionContext.SESSION_NAME)
            call.respondRedirect(url = RbacLoginView.RBAC_LOGIN_PATH)
        }
    }
}

