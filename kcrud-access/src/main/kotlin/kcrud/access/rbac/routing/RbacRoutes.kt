/*
 * Copyright (c) 2023-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.access.rbac.routing

import io.ktor.server.http.content.*
import io.ktor.server.routing.*
import kcrud.access.rbac.plugin.annotation.RbacAPI
import kcrud.access.rbac.routing.admin.rbacAdminRoute
import kcrud.access.rbac.routing.login.rbacLoginRoute
import kcrud.access.rbac.routing.login.rbacLogoutRoute
import kcrud.base.utils.NetworkUtils

/**
 * Contains the RBAC endpoints.
 */
@OptIn(RbacAPI::class)
fun Route.rbacRoute() {

    // Required so the HTML for can find its respective CSS file.
    staticResources(remotePath = "/static-rbac", basePackage = "rbac")

    rbacLoginRoute()

    rbacLogoutRoute()

    rbacAdminRoute()

    NetworkUtils.logEndpoints(
        reason = "RBAC",
        endpoints = listOf("rbac/login")
    )
}
