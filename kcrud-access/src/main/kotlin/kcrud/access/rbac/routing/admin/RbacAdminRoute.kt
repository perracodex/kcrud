/*
 * Copyright (c) 2023-Present Perracodex. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.access.rbac.routing.admin

import io.ktor.server.routing.*
import kcrud.access.rbac.plugin.annotation.RbacAPI
import kcrud.access.rbac.plugin.withRbac
import kcrud.access.rbac.service.RbacService
import kcrud.base.database.schema.admin.rbac.types.RbacAccessLevel
import kcrud.base.database.schema.admin.rbac.types.RbacScope
import org.koin.ktor.ext.inject

/**
 * Defines all the RBAC admin routes.
 */
@OptIn(RbacAPI::class)
fun Route.rbacAdminRoute() {
    val rbacService: RbacService by inject()

    withRbac(scope = RbacScope.RBAC_ADMIN, accessLevel = RbacAccessLevel.VIEW) {
        rbacAdminRouteGet(rbacService = rbacService)
        rbacSAdminRoutePost(rbacService = rbacService)
    }
}
