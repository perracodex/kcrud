/*
 * Copyright (c) 2023-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
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
import kcrud.access.rbac.service.RbacService
import kcrud.access.rbac.views.RbacAdminView
import kcrud.access.rbac.views.RbacLoginView
import kcrud.base.database.schema.admin.rbac.types.RbacAccessLevel
import kcrud.base.database.schema.admin.rbac.types.RbacResource
import kcrud.base.env.SessionContext
import org.koin.java.KoinJavaComponent.getKoin

@RbacAPI
fun Route.rbacLoginRoute() {

    get("rbac/login") {
        if (getRbacAdminAccessActor(call = call) == null) {
            call.respondHtml(status = HttpStatusCode.OK) {
                RbacLoginView.build(html = this)
            }
        } else {
            call.respondRedirect(url = RbacAdminView.RBAC_ADMIN_PATH)
        }
    }

    authenticate(RbacLoginView.RBAC_LOGIN_PATH) {
        post("rbac/login") {
            call.respondRedirect(url = RbacAdminView.RBAC_ADMIN_PATH)
        }
    }
}

@RbacAPI
suspend fun getRbacAdminAccessActor(call: ApplicationCall): SessionContext? {
    val sessionContext: SessionContext? = call.sessions.get<SessionContext>()

    if (sessionContext != null) {
        val rbacService: RbacService = getKoin().get()

        val hasPermission: Boolean = rbacService.hasPermission(
            sessionContext = sessionContext,
            resource = RbacResource.RBAC_ADMIN,
            accessLevel = RbacAccessLevel.VIEW
        )

        if (hasPermission) {
            return sessionContext
        }

        call.sessions.clear(name = SessionContext.SESSION_NAME)
    }

    return null
}
