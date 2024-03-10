/*
 * Copyright (c) 2023-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.server.plugins

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.routing.*
import kcrud.base.admin.rbac.routing.rbacRoute
import kcrud.base.admin.system.systemRoute
import kcrud.base.infrastructure.health.routing.healthCheckRoute
import kcrud.base.plugins.RateLimitScope
import kcrud.base.scheduler.routing.quartzRoutes
import kcrud.base.security.routing.accessTokenRoute
import kcrud.base.settings.AppSettings
import kcrud.domain.employee.routing.employeeRoute
import kcrud.domain.employment.routing.employmentRoute
import kcrud.server.demo.employeesDemoRoute
import kotlinx.serialization.json.Json

/**
 * Initializes and sets up routing for the application.
 *
 * Routing is the core Ktor plugin for handling incoming requests in a server application.
 * When the client makes a request to a specific URL (for example, /hello), the routing
 * mechanism allows us to define how we want this request to be served.
 *
 * See: [Ktor Routing Documentation](https://ktor.io/docs/routing-in-ktor.html)
 *
 * See [Application Structure](https://ktor.io/docs/structuring-applications.html) for examples
 * of how to organize routes in diverse ways.
 *
 * See: [Content negotiation and serialization](https://ktor.io/docs/serialization.html#0)
 *
 * See: [Kotlin Serialization Guide](https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/serialization-guide.md)
 *
 * See: [Ktor Rate Limit](https://ktor.io/docs/rate-limit.html)
 */
fun Application.configureRoutes() {

    routing {

        // The ContentNegotiation plugin is set at the routing level rather than
        // at the application level to prevent potential conflicts that could arise
        // if other libraries also attempt to install their own ContentNegotiation plugin,
        // which would result in a DuplicatePluginException.
        install(plugin = ContentNegotiation) {
            // Define the behavior and characteristics for JSON serialization.
            json(Json {
                prettyPrint = true         // Format JSON output for easier reading.
                encodeDefaults = true      // Serialize properties with default values.
                ignoreUnknownKeys = false  // Fail on unknown keys in the incoming JSON.
            })
        }

        val isSecurityOptional: Boolean = !AppSettings.security.isEnabled

        // Define domain business routes.
        rateLimit(configuration = RateLimitName(name = RateLimitScope.PUBLIC_API.key)) {
            authenticate(AppSettings.security.jwt.providerName, optional = isSecurityOptional) {
                employeeRoute()
                employmentRoute()
            }
        }

        // Employees demo route.
        authenticate(AppSettings.security.basic.providerName, optional = isSecurityOptional) {
            employeesDemoRoute()
        }

        accessTokenRoute()
        healthCheckRoute()
        systemRoute()
        rbacRoute()
        quartzRoutes()
    }
}
