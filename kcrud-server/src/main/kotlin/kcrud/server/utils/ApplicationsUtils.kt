/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.server.utils

import io.ktor.server.application.*
import kcrud.base.env.Tracer
import kcrud.base.security.snowflake.SnowflakeFactory
import kcrud.base.settings.AppSettings
import kcrud.base.utils.NetworkUtils

/**
 * Utility functions for the application server.
 */
internal object ApplicationsUtils {
    private val tracer = Tracer<ApplicationsUtils>()

    /**
     * Watches the server for readiness and logs the server's endpoints to the console.
     */
    fun watchServer(environment: ApplicationEnvironment) {
        environment.monitor.subscribe(definition = ServerReady) {

            // Dumps the server's endpoints to the console for easy access and testing.
            // This does not include the actual API routes endpoints.
            NetworkUtils.logEndpoints(reason = "Demo", endpoints = listOf("demo"))
            NetworkUtils.logEndpoints(reason = "Healthcheck", endpoints = listOf("health"))
            NetworkUtils.logEndpoints(reason = "Snowflake", endpoints = listOf("snowflake/${SnowflakeFactory.nextId()}"))
            NetworkUtils.logEndpoints(reason = "Scheduler", endpoints = listOf("scheduler/dashboard"))
            NetworkUtils.logEndpoints(reason = "Micrometer Metrics", endpoints = listOf("metrics"))

            if (AppSettings.security.rbac.isEnabled) {
                NetworkUtils.logEndpoints(reason = "RBAC", endpoints = listOf("rbac/login"))
            }

            if (AppSettings.apiSchema.environments.contains(AppSettings.runtime.environment)) {
                NetworkUtils.logEndpoints(
                    reason = "Swagger, Redoc, OpenApi",
                    endpoints = listOf(
                        AppSettings.apiSchema.swaggerEndpoint,
                        AppSettings.apiSchema.redocEndpoint,
                        AppSettings.apiSchema.openApiEndpoint,
                    )
                )
            }

            // Log the server readiness.
            tracer.withSeverity("Development Mode Enabled: ${environment.developmentMode}.")
            tracer.info("Server configured. Environment: ${AppSettings.runtime.environment}.")
        }
    }
}
