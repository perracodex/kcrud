/*
 * Copyright (c) 2023-Present Perracodex. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.server.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kcrud.access.rbac.routing.rbacRoute
import kcrud.access.token.accessTokenRoute
import kcrud.base.env.SessionContext
import kcrud.base.env.health.routing.healthCheckRoute
import kcrud.base.plugins.RateLimitScope
import kcrud.base.scheduling.routing.adminSchedulerRoutes
import kcrud.base.security.snowflake.snowflakeRoute
import kcrud.base.settings.AppSettings
import kcrud.domain.employee.routing.employeeRoute
import kcrud.domain.employment.routing.employmentRoute
import kcrud.server.demo.employeesDemoRoute

/**
 * Initializes and sets up routing for the application.
 *
 * Routing is the core Ktor plugin for handling incoming requests in a server application.
 * When the client makes a request to a specific URL (for example, /hello), the routing
 * mechanism allows us to define how we want this request to be served.
 *
 * See: [Ktor Routing Documentation](https://ktor.io/docs/server-routing.html)
 *
 * See [Application Structure](https://ktor.io/docs/server-application-structure.html) for examples
 * of how to organize routes in diverse ways.
 *
 * See: [Ktor Rate Limit](https://ktor.io/docs/server-rate-limit.html)
 */
fun Application.configureRoutes() {

    routing {

        // Define domain routes.
        rateLimit(configuration = RateLimitName(name = RateLimitScope.PUBLIC_API.key)) {
            authenticate(AppSettings.security.jwt.providerName, optional = !AppSettings.security.isEnabled) {
                employeeRoute()
                employmentRoute()
            }
        }

        // Demo route.
        authenticate(AppSettings.security.basic.providerName, optional = !AppSettings.security.isEnabled) {
            employeesDemoRoute()
        }

        accessTokenRoute()
        healthCheckRoute()
        snowflakeRoute()
        rbacRoute()
        adminSchedulerRoutes()

        // Server root endpoint.
        get("/") {
            val sessionContext: SessionContext? = call.principal<SessionContext>()
            sessionContext?.let {
                call.respondText(text = "Hello World. Welcome ${it.username}!")
            } ?: call.respondText(text = "Hello World.")
        }
    }
}
