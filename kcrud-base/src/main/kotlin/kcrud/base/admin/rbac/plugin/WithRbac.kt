/*
 * Copyright (c) 2023-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.admin.rbac.plugin

import io.ktor.server.routing.*
import kcrud.base.admin.rbac.plugin.annotation.RbacAPI
import kcrud.base.database.schema.admin.rbac.types.RbacAccessLevel
import kcrud.base.database.schema.admin.rbac.types.RbacResource
import kcrud.base.settings.AppSettings

/**
 * DSL function for applying RBAC authorization to Ktor routes.
 *
 * @param resource The RBAC resource associated with the route, defining the scope of access control.
 * @param accessLevel The RBAC access level required for accessing the route, defining the degree of access control.
 * @param build The routing logic to be applied within the RBAC-authorized route. This is the endpoint block.
 * @return The created Route object configured with RBAC constraints.
 */
@OptIn(RbacAPI::class)
fun Route.withRbac(resource: RbacResource, accessLevel: RbacAccessLevel, build: Route.() -> Unit): Route {
    return if (AppSettings.security.rbac.isEnabled)
        rbacAuthorizedRoute(resource = resource, accessLevel = accessLevel, build = build)
    else
        this.apply(build)
}
