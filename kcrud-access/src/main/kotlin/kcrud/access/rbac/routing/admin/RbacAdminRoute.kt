/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.access.rbac.routing.admin

import io.ktor.server.routing.*
import kcrud.access.rbac.plugin.annotation.RbacAPI
import kcrud.access.rbac.plugin.withRbac
import kcrud.base.database.schema.admin.rbac.types.RbacAccessLevel
import kcrud.base.database.schema.admin.rbac.types.RbacScope

/**
 * Defines all the RBAC admin routes.
 */
@OptIn(RbacAPI::class)
internal fun Route.rbacAdminRoute() {
    withRbac(scope = RbacScope.RBAC_ADMIN, accessLevel = RbacAccessLevel.VIEW) {
        rbacAdminRouteGet()
        rbacAdminRoutePost()
    }
}
