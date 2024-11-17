/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.core.env.health

import io.github.perracodex.kopapi.dsl.operation.api
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kcrud.core.settings.AppSettings

/**
 * Defines the health check endpoints.
 *
 * The current implementation checks the basic readiness of the application. Future
 * enhancements could include more complex health checks, like database connectivity,
 * external service availability, or other critical component checks.
 */
public fun Route.healthCheckRoute() {
    authenticate(AppSettings.security.basicAuth.providerName, optional = !AppSettings.security.isEnabled) {
        get("/admin/health") {
            val healthCheck: HealthCheck = HealthCheck.create(call = call)
            call.respond(status = HttpStatusCode.OK, message = healthCheck)
        } api {
            tags = setOf("System")
            summary = "Health check."
            description = "Provides the current operational status."
            operationId = "healthCheck"
            response<HealthCheck>(status = HttpStatusCode.OK) {
                description = "The health check status."
            }
            basicSecurity(name = "System") {
                description = "Access to system information."
            }
        }
    }
}
