/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.server.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kcrud.access.rbac.api.rbacRoutes
import kcrud.access.token.api.accessTokenRoutes
import kcrud.core.env.SessionContext.Companion.getContext
import kcrud.core.env.health.healthCheckRoute
import kcrud.core.events.sseRoute
import kcrud.core.plugins.RateLimitScope
import kcrud.core.scheduler.api.schedulerRoutes
import kcrud.core.security.snowflake.snowflakeRoute
import kcrud.core.settings.AppSettings
import kcrud.domain.employee.api.employeeRoutes
import kcrud.domain.employment.api.employmentRoutes
import kcrud.server.demo.api.demoRoutes

/**
 * Initializes and sets up routing for the application.
 *
 * Routing is the core Ktor plugin for handling incoming requests in a server application.
 * When the client makes a request to a specific URL (for example, /hello), the routing
 * mechanism allows us to define how we want this request to be served.
 *
 * #### References
 * - [Ktor Routing Documentation](https://ktor.io/docs/server-routing.html)
 * - [Application Structure](https://ktor.io/docs/server-application-structure.html) for examples
 * of how to organize routes in diverse ways.
 * - [Ktor Rate Limit](https://ktor.io/docs/server-rate-limit.html)
 */
internal fun Application.configureRoutes() {

    routing {

        // Domain routes.
        rateLimit(configuration = RateLimitName(name = RateLimitScope.PUBLIC_API.key)) {
            authenticate(AppSettings.security.jwtAuth.providerName, optional = !AppSettings.security.isEnabled) {
                employeeRoutes()
                employmentRoutes()
            }
        }

        // Demo route.
        authenticate(AppSettings.security.basicAuth.providerName, optional = !AppSettings.security.isEnabled) {
            demoRoutes()
        }

        accessTokenRoutes()
        healthCheckRoute()
        snowflakeRoute()
        rbacRoutes()
        schedulerRoutes()
        sseRoute()

        // Server root endpoint.
        get("/") {
            call.getContext()?.let {
                call.respondText(text = "Hello World. Welcome ${it.username}!")
            } ?: call.respondText(text = "Hello World.")
        }
    }
}
