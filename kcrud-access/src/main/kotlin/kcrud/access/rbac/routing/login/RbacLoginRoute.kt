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
import kcrud.access.rbac.service.RbacService
import kcrud.access.rbac.view.RbacAdminView
import kcrud.access.rbac.view.RbacLoginView
import kcrud.base.database.schema.admin.rbac.types.RbacAccessLevel
import kcrud.base.database.schema.admin.rbac.types.RbacScope
import kcrud.base.env.SessionContext
import org.koin.java.KoinJavaComponent.getKoin

/**
 * The route for logging into the RBAC admin panel.
 */
@RbacAPI
internal fun Route.rbacLoginRoute() {

    get("rbac/login") {
        getRbacAdminAccessActor(call = call)?.let {
            // If the actor is not null, redirect to the admin view.
            call.respondRedirect(url = RbacAdminView.RBAC_ADMIN_PATH)
        } ?: run {
            // If the actor is null, show the login view.
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

/**
 * Get the [SessionContext] if the user has access to the RBAC admin panel.
 * If the user does not have access, the session is cleared and null is returned.
 *
 * @param call The application call.
 * @return The [SessionContext] if the user has access to the RBAC admin panel, null otherwise.
 */
@RbacAPI
internal suspend fun getRbacAdminAccessActor(call: ApplicationCall): SessionContext? {
    val sessionContext: SessionContext? = call.sessions.get<SessionContext>()

    sessionContext?.let {
        val rbacService: RbacService = getKoin().get()

        val hasPermission: Boolean = rbacService.hasPermission(
            sessionContext = sessionContext,
            scope = RbacScope.RBAC_ADMIN,
            accessLevel = RbacAccessLevel.VIEW
        )

        if (hasPermission) {
            return sessionContext
        }

        call.sessions.clear(name = SessionContext.SESSION_NAME)
    }

    return null
}
