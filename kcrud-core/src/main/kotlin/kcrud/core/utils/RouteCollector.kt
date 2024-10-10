/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.core.utils

import io.ktor.server.application.*
import io.ktor.server.routing.*
import kcrud.core.env.health.annotation.HealthCheckAPI
import kotlinx.serialization.Serializable

/**
 * Extension function traverse and collect all routes.
 *
 * @return A list of all found routes.
 */
@HealthCheckAPI
internal fun Application.collectRoutes(): List<RouteInfo> {
    val routes: MutableList<RouteInfo> = mutableListOf()

    // Helper function to recursively traverse routes and collect attribute values.
    fun Route.collectAttributes() {
        when (this) {
            is RoutingRoot -> {
                this.children.forEach { it.collectAttributes() }
            }

            is RoutingNode -> {
                if (this.selector is HttpMethodRouteSelector) {
                    val method: String = (this.selector as HttpMethodRouteSelector).method.value
                    val path: String = this.extractEndpointPath()
                    val routeInfo = RouteInfo(path = path, method = method)
                    routes.add(routeInfo)
                }
                this.children.forEach { it.collectAttributes() }
            }
        }
    }

    // Start collecting from the root route.
    this.routing { }.collectAttributes()

    return routes.sortedWith(
        compareBy(
            { it.path },
            { it.method }
        )
    )
}

/**
 * Holds information about a registered route.
 *
 * @property path The path of the route.
 * @property method The HTTP method of the route.
 */
@Serializable
public data class RouteInfo(val path: String, val method: String)

/**
 * Constructs the full path of a route by aggregating path segments from the current route up to the root,
 * by traversing the parent chain of the current route and collects segments defined by various
 * types of [RouteSelector] types.
 *
 * It builds a full path by piecing together these segments in the order from the root to the current route.
 * The method is designed to ignore segments that do not directly contribute to the path structure,
 * such as HTTP method selectors and trailing slashes.
 *
 * @return A string representing the full path from the root to the current route, starting with a `/`.
 * If the current route is at the root or no path segments are defined, the function returns just `/`.
 */
internal fun Route.extractEndpointPath(): String {
    val segments: MutableList<String> = mutableListOf()
    var currentRoute: Route? = this

    // Traverse the parent chain of the current route and collect path segments.
    while (currentRoute != null) {
        val selector: RouteSelector = when (this) {
            is RoutingRoot -> this.selector
            is RoutingNode -> this.selector
            else -> break
        }

        val segment: String = when (selector) {
            is PathSegmentConstantRouteSelector -> selector.value
            is PathSegmentParameterRouteSelector -> "{${selector.name}}"
            is PathSegmentOptionalParameterRouteSelector -> "{${selector.name}?}"
            is PathSegmentWildcardRouteSelector -> "*"
            is TrailingSlashRouteSelector -> ""
            is HttpMethodRouteSelector -> "" // Skip HTTP method selectors
            else -> ""
        }
        if (segment.isNotEmpty()) {
            segments.add(segment)
        }
        currentRoute = currentRoute.parent
    }

    return segments.reversed().joinToString(separator = "/", prefix = "/").trim()
}
