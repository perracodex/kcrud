/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.access.domain.rbac.api

import io.ktor.server.http.content.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.routing.*
import kcrud.access.domain.rbac.api.dashboard.rbacDashboardLoadRoute
import kcrud.access.domain.rbac.api.dashboard.rbacDashboardUpdateRoute
import kcrud.access.domain.rbac.api.login.rbacLoginAccessRoute
import kcrud.access.domain.rbac.api.login.rbacLoginSubmissionRoute
import kcrud.access.domain.rbac.api.login.rbacLogoutRoute
import kcrud.access.domain.rbac.plugin.annotation.RbacApi
import kcrud.access.domain.rbac.plugin.withRbac
import kcrud.core.plugins.RateLimitScope
import kcrud.database.schema.admin.rbac.type.RbacAccessLevel
import kcrud.database.schema.admin.rbac.type.RbacScope

/**
 * Contains the RBAC endpoints.
 *
 * These include the login and logout routes, as well as the dashboard routes.
 */
@OptIn(RbacApi::class)
public fun Route.rbacRoutes() {

    // Configures the server to serve CSS files located in the 'rbac' resources folder,
    // necessary for styling the RBAC dashboard built with HTML DSL.
    staticResources(remotePath = "/static-rbac", basePackage = "rbac")

    rateLimit(configuration = RateLimitName(name = RateLimitScope.PRIVATE_API.key)) {
        rbacLoginAccessRoute()
        rbacLoginSubmissionRoute()
        rbacLogoutRoute()

        withRbac(scope = RbacScope.RBAC_DASHBOARD, accessLevel = RbacAccessLevel.VIEW) {
            rbacDashboardLoadRoute()
            rbacDashboardUpdateRoute()
        }
    }
}
