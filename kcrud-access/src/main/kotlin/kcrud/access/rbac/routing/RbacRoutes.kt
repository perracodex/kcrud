/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.access.rbac.routing

import io.ktor.server.http.content.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.routing.*
import kcrud.access.rbac.plugin.annotation.RbacAPI
import kcrud.access.rbac.routing.admin.rbacAdminRoute
import kcrud.access.rbac.routing.login.rbacLoginRoute
import kcrud.access.rbac.routing.login.rbacLogoutRoute
import kcrud.base.plugins.RateLimitScope

/**
 * Contains the RBAC endpoints.
 */
@OptIn(RbacAPI::class)
fun Route.rbacRoute() {

    // Required so the HTML for can find its respective CSS file.
    staticResources(remotePath = "/static-rbac", basePackage = "rbac")

    rateLimit(configuration = RateLimitName(name = RateLimitScope.PRIVATE_API.key)) {
        rbacLoginRoute()
        rbacLogoutRoute()
        rbacAdminRoute()
    }
}
