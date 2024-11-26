/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.access.domain.rbac.api.login

import io.github.perracodex.kopapi.dsl.operation.api
import io.ktor.http.*
import io.ktor.server.html.*
import io.ktor.server.routing.*
import kcrud.access.domain.rbac.plugin.annotation.RbacApi
import kcrud.access.domain.rbac.view.RbacLoginView
import kcrud.core.context.SessionContext
import kcrud.core.context.clearContext

/**
 * Manages access to the RBAC login page. If a valid [SessionContext] is already exists, the actor
 * is directly redirected to the dashboard. Otherwise, any existing session cookies are
 * cleared and the login page is presented.
 */
@RbacApi
internal fun Route.rbacLoginAccessRoute() {
    get("/rbac/login") {
        call.clearContext()
        call.respondHtml(status = HttpStatusCode.OK) {
            RbacLoginView.build(html = this)
        }
    } api {
        tags = setOf("RBAC")
        summary = "Access the RBAC login page."
        description = "Access the RBAC login page to authenticate and login."
        operationId = "rbacLoginAccess"
        response<String>(status = HttpStatusCode.OK) {
            description = "The RBAC login page."
        }
    }
}
