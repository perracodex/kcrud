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
 * 1. Child Route Creation: A new route node is created to which RBAC constraints will be applied.
 *
 * 2. Plugin Installation: The 'RbacPlugin' is installed to the new child route node. During installation,
 *    the plugin is configured with a target RBAC resource and access level. This association ensures that
 *    the RBAC logic defined in the plugin is applied only to the new route, allowing the trigger of its
 *    enclosed build block only if the plugin authorizes it.
 *
 * 3. Route Configuration: The 'build' block is bound on the new child route. This build block is where
 *    the actual source route logic (like handling endpoints) is defined. Importantly, all route handlers
 *    within the build block are subject to the RBAC constraints handled by the created RBAC route node.
 *
 * @param resource The RBAC resource associated with the route, defining the scope of access control.
 * @param accessLevel The RBAC access level required for accessing the route, defining the degree of access control.
 * @param build The lambda function defining the route's handling logic that must adhere to the RBAC constraints.
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

    // Create the selector to be used to tag routes for RBAC, acting only as a marker in the routing structure.
    // It does not modify how routes are selected but signifies routes that require RBAC checks.
    val authorizedSelector = AuthorizedRouteSelector(resource = resource, accessLevel = accessLevel)

    // Create the new route node marked with the new selector, to which the RBAC plugin will be applied.
    val authorizedRoute: Route = createChild(selector = authorizedSelector)

    // Install the 'RbacPlugin' on the newly created child route. During this installation, the plugin
    // is configured with the same RBAC resource and access level that were used to create the route.
    // This ensures the enforcement of RBAC policies on the route, making certain that access is granted
    // only to actors with the appropriate permissions defined by the resource and access level parameters.
    authorizedRoute.install(RbacPlugin) {
        this.resource = resource
        this.accessLevel = accessLevel
    }

    // Apply the 'build' block to the new RBAC route, registering its route configurations and handlers
    // within the context of the RBAC constraints. This sets up the route's structure and behavior according
    // to the defined logic in 'build', but does not execute the request handling logic immediately.
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
