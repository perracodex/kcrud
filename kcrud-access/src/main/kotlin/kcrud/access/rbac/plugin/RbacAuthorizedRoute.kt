/*
 * Copyright (c) 2023-Present Perraco. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.access.rbac.plugin

import io.ktor.server.application.*
import io.ktor.server.routing.*
import kcrud.access.rbac.plugin.annotation.RbacAPI
import kcrud.base.database.schema.admin.rbac.types.RbacAccessLevel
import kcrud.base.database.schema.admin.rbac.types.RbacScope

/**
 * Creates an RBAC-authorized route within the Ktor routing structure. This function is a key part
 * of enforcing Role-Based Access Control (RBAC) on specific routes.
 *
 * @param scope The RBAC scope associated with the route, defining the scope of access control.
 * @param accessLevel The RBAC access level required for accessing the route, defining the degree of access control.
 * @param build The lambda function defining the route's handling logic that must adhere to the RBAC constraints.
 * @return The created Route object configured with RBAC constraints.
 */
@RbacAPI
internal fun Route.rbacAuthorizedRoute(
    scope: RbacScope,
    accessLevel: RbacAccessLevel,
    build: Route.() -> Unit,
): Route {
    if (accessLevel == RbacAccessLevel.NONE) {
        throw IllegalArgumentException(
            "'${accessLevel.name}' access level is not meant to be used as mark for scopes access. " +
                    "When defining a RBAC route its target scope must always have a minimum access level. " +
                    "'${accessLevel.name}' can be used only to restrict a role to a concrete scope."
        )
    }

    // Create the selector to be used to tag routes for RBAC, acting only as a marker in the routing structure.
    // It does not modify how routes are selected but signifies routes that require RBAC checks.
    val authorizedSelector = AuthorizedRouteSelector(scope = scope, accessLevel = accessLevel)

    // Create the new route node marked with the new selector, to which the RBAC plugin will be applied.
    val authorizedRoute: Route = createChild(selector = authorizedSelector)

    // Install the 'RbacPlugin' on the newly created child route. During this installation, the plugin
    // is configured with the same RBAC scope and access level that were used to create the route.
    // This ensures the enforcement of RBAC policies on the route, making certain that access is granted
    // only to actors with the appropriate permissions defined by the scope and access level parameters.
    authorizedRoute.install(plugin = RbacPlugin) {
        this.scope = scope
        this.accessLevel = accessLevel
    }

    // Apply the 'build' block to the new RBAC route, registering its route configurations and handlers
    // within the context of the RBAC constraints. This sets up the route's structure and behavior according
    // to the defined logic in 'build', but does not execute the request handling logic immediately.
    authorizedRoute.build()

    return authorizedRoute
}

/**
 * A custom route selector to mark routes that have specific RBAC constraints based on a scope and access level.
 * It doesn't directly influence the routing logic but serves as a marker to identify routes with these constraints.
 *
 * The 'evaluate' function returns a constant evaluation, indicating that this selector does not participate
 * in the route resolution process but is used solely for marking purposes.
 *
 * @property scope The RBAC scope associated with the route, defining the scope of access control.
 * @property accessLevel The RBAC access level required for accessing the route, defining the degree of access control.
 */
private class AuthorizedRouteSelector(
    private val scope: RbacScope,
    private val accessLevel: RbacAccessLevel
) : RouteSelector() {
    override fun evaluate(context: RoutingResolveContext, segmentIndex: Int): RouteSelectorEvaluation {
        return RouteSelectorEvaluation.Constant
    }

    override fun toString(): String = "Authorize: Scope=$scope, Level=$accessLevel"
}
