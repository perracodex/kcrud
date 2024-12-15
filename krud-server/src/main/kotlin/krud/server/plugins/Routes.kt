/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package krud.server.plugins

import io.github.perracodex.kopapi.dsl.operation.api
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import krud.access.domain.rbac.api.rbacRoutes
import krud.access.domain.token.api.accessTokenRoutes
import krud.base.context.getContextOrNull
import krud.base.event.sseRoutes
import krud.base.plugins.RateLimitScope
import krud.base.security.snowflake.snowflakeRoute
import krud.base.settings.AppSettings
import krud.domain.employee.api.employeeRoutes
import krud.domain.employment.api.employmentRoutes
import krud.server.demo.api.demoRoutes
import krud.server.health.healthCheckRoute

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
        sseRoutes()

        // Server root endpoint.
        get("/") {
            val greeting: String = call.getContextOrNull()?.let { sessionContext ->
                "Hello World. Welcome ${sessionContext.username}!"
            } ?: "Hello World."
            call.respondText(text = greeting)
        } api {
            tags = setOf("Root")
            summary = "Root endpoint."
            description = "The root endpoint of the server."
            operationId = "root"
            response<String>(status = HttpStatusCode.OK) {
                description = "Root endpoint response."
            }
        }
    }
}
