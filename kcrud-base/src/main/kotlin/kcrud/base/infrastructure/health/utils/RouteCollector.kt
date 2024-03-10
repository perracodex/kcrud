/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.infrastructure.health.utils

import io.ktor.server.application.*
import io.ktor.server.routing.*
import kcrud.base.infrastructure.health.annotation.HealthCheckAPI

/**
 * Collects all the routes defined in the application.
 *
 * @return A list of strings representing the registered routes.
 */
@HealthCheckAPI
fun Application.collectRoutes(): List<String> {
    return routing {}.allRoutes()
}

/**
 * Recursively collects all the terminal routes from the current route and its children.
 * Terminal routes are those that directly handle requests and are not just path prefixes.
 *
 * @param prefix The prefix to be used for the path of each route.
 *               It accumulates as it goes deeper into the route tree.
 * @return A list of strings representing the terminal routes.
 */
private fun Route.allRoutes(prefix: String = ""): List<String> {
    val routes: MutableList<String> = mutableListOf()

    /**
     * Checks if the route is a prefix (non-terminal) route.
     * A prefix route is identified by having children with specific HTTP method selectors.
     *
     * @return True if the route is a prefix route, false otherwise.
     */
    fun Route.isPrefixRoute(): Boolean {
        return this.children.any { it.selector is HttpMethodRouteSelector }
    }

    /**
     * Recursive function to traverse the route tree and collect terminal routes.
     *
     * @param currentPath The current path accumulated from the parent routes.
     */
    fun Route.collectRoutes(currentPath: String) {
        val pathSegment: String = when (val selector = this.selector) {
            is PathSegmentConstantRouteSelector -> "${currentPath}/${selector.value}"
            is PathSegmentParameterRouteSelector -> "${currentPath}/{${selector.name}}"
            is PathSegmentWildcardRouteSelector -> "${currentPath}/*"
            else -> currentPath
        }

        // Add the route if it's a terminal route (has an HttpMethodRouteSelector and is not a prefix route).
        if (this.selector is HttpMethodRouteSelector && !this.isPrefixRoute()) {
            val method: String = (this.selector as HttpMethodRouteSelector).method.value
            routes.add("$method $pathSegment")
        }

        // Recursively collect routes from children.
        this.children.forEach { it.collectRoutes(currentPath = pathSegment) }
    }

    this.collectRoutes(currentPath = prefix)

    return routes.distinct()
}
