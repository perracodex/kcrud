/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.access.rbac.api.login

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.routing.*
import kcrud.access.rbac.plugin.annotation.RbacAPI
import kcrud.access.rbac.view.RbacLoginView
import kcrud.core.context.SessionContext
import kcrud.core.context.clearContext

/**
 * Manages access to the RBAC login page. If a valid [SessionContext] is already exists, the actor
 * is directly redirected to the dashboard. Otherwise, any existing session cookies are
 * cleared and the login page is presented.
 */
@RbacAPI
internal fun Route.rbacLoginAccessRoute() {
    /**
     * Opens the RBAC login page. If a valid [SessionContext] is present,
     * it gets cleared and the actor is redirected to the login screen.
     * @OpenAPITag RBAC
     */
    get("rbac/login") {
        call.clearContext()
        call.respondHtml(status = HttpStatusCode.OK) {
            RbacLoginView.build(html = this)
        }
    }
}
