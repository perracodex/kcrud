/*
 * Copyright (c) 2023-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.access.rbac.plugin

import io.ktor.server.application.*
import io.ktor.server.routing.*
import kcrud.access.rbac.plugin.annotation.RbacAPI
import kcrud.base.database.schema.admin.rbac.types.RbacAccessLevel
import kcrud.base.database.schema.admin.rbac.types.RbacResource

/**
 * Creates an RBAC-authorized route within the Ktor routing structure. This function is a key part
 * of enforcing Role-Based Access Control (RBAC) on specific routes.
 *
 * How it works:
 * 1. Child Route Creation: Utilizes 'createChild' along with a custom 'AuthorizedRouteSelector'.
 *    This selector is crucial as it carries the RBAC resource and access level information, effectively
 *    tagging the route with these specific access control parameters.
 *
 * 2. Plugin Installation: The 'RbacPlugin' is installed on this child route. During installation,
 *    the plugin is configured with the RBAC resource and access level. This association ensures that
 *    the RBAC logic defined in the plugin is applied only to this route, allowing the trigger of its
 *    enclosed endpoints only if the plugin authorizes it.
 *
 * 3. Route Configuration: The lambda function 'build' is executed on this child route. This block
 *    is where the actual route logic (like handling GET/POST endpoints) is defined. Importantly,
 *    all route handlers within this block are subject to the RBAC constraints set earlier.
 *
 * @param resource The RBAC resource associated with the route, defining the scope of access control.
 * @param accessLevel The RBAC access level required for accessing the route, defining the degree of access control.
 * @param build The lambda function to define the route's handling logic.
 * @return The created Route object configured with RBAC constraints.
 */
@RbacAPI
internal fun Route.rbacAuthorizedRoute(
    resource: RbacResource,
    accessLevel: RbacAccessLevel,
    build: Route.() -> Unit,
): Route {
    if (accessLevel == RbacAccessLevel.NONE) {
        throw IllegalArgumentException(
            "'${accessLevel.name}' access level is not meant to be used as mark for resources access. " +
                    "When defining a RBAC route its target resource must always have a minimum access level. " +
                    "'${accessLevel.name}' can be used only to restrict a role to a concrete resource."
        )
    }

    val authorizedSelector = AuthorizedRouteSelector(resource = resource, accessLevel = accessLevel)
    val authorizedRoute: Route = createChild(selector = authorizedSelector)

    authorizedRoute.install(RbacPlugin) {
        this.resource = resource
        this.accessLevel = accessLevel
    }

    authorizedRoute.build()
    return authorizedRoute
}

/**
 * A custom route selector to mark routes that have specific RBAC constraints based on a resource and access level.
 * It doesn't directly influence the routing logic but serves as a marker to identify routes with these constraints.
 *
 * The 'evaluate' function returns a constant evaluation, indicating that this selector does not participate
 * in the route resolution process but is used solely for marking purposes.
 *
 * @param resource The RBAC resource associated with the route, defining the scope of access control.
 * @param accessLevel The RBAC access level required for accessing the route, defining the degree of access control.
 */
private class AuthorizedRouteSelector(
    private val resource: RbacResource,
    private val accessLevel: RbacAccessLevel
) : RouteSelector() {
    override fun evaluate(context: RoutingResolveContext, segmentIndex: Int): RouteSelectorEvaluation {
        return RouteSelectorEvaluation.Constant
    }

    override fun toString(): String = "Authorize: Resource=$resource, Level=$accessLevel"
}
