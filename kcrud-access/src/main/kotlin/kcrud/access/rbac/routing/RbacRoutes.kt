/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.access.rbac.routing

import io.ktor.server.http.content.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.routing.*
import kcrud.access.rbac.plugin.annotation.RbacAPI
import kcrud.access.rbac.plugin.withRbac
import kcrud.access.rbac.routing.dashboard.rbacDashboardLoadRoute
import kcrud.access.rbac.routing.dashboard.rbacDashboardUpdateRoute
import kcrud.access.rbac.routing.login.rbacLoginRoute
import kcrud.access.rbac.routing.login.rbacLogoutRoute
import kcrud.base.database.schema.admin.rbac.types.RbacAccessLevel
import kcrud.base.database.schema.admin.rbac.types.RbacScope
import kcrud.base.plugins.RateLimitScope

/**
 * Contains the RBAC endpoints.
 *
 * These include the login and logout routes, as well as the dashboard routes.
 */
@OptIn(RbacAPI::class)
public fun Route.rbacRoute() {

    // Configures the server to serve CSS files located in the 'rbac' resources folder,
    // necessary for styling the RBAC dashboard built with HTML DSL.
    staticResources(remotePath = "/static-rbac", basePackage = "rbac")

    rateLimit(configuration = RateLimitName(name = RateLimitScope.PRIVATE_API.key)) {
        rbacLoginRoute()
        rbacLogoutRoute()

        withRbac(scope = RbacScope.RBAC_DASHBOARD, accessLevel = RbacAccessLevel.VIEW) {
            rbacDashboardLoadRoute()
            rbacDashboardUpdateRoute()
        }
    }
}
