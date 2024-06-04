/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.server.utils

import io.github.cdimascio.dotenv.DotenvException
import io.github.cdimascio.dotenv.dotenv
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
     * Loads environment variables from the project `.env` file and sets them as system properties.
     * This allows the application to seamlessly access these variables without needing explicit
     * creation at the OS level.
     *
     * This method must be called before the server is started to ensure that when [AppSettings]
     * is initialized, it properly incorporates these environment variables into the server configuration.
     * This is crucial because the server configuration utilizes placeholders that are replaced by
     * these environment variables.
     */
    fun loadEnvironmentVariables() {
        tracer.info("Loading environment variables from '.env' file.")

        try {
            val dotenv = dotenv()

            dotenv.entries().forEach { entry ->
                System.setProperty(entry.key, entry.value)
            }
        } catch (e: DotenvException) {
            tracer.info("No '.env' file found. Defaulting to system environment variables.")
        }
    }

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
            NetworkUtils.logEndpoints(reason = "RBAC", endpoints = listOf("rbac/login"))
            NetworkUtils.logEndpoints(reason = "Scheduler", endpoints = listOf("scheduler/tasks/dashboard"))
            NetworkUtils.logEndpoints(reason = "Micrometer Metrics", endpoints = listOf("metrics"))
            NetworkUtils.logEndpoints(
                reason = "Swagger, Redoc, OpenApi",
                endpoints = listOf(
                    AppSettings.apiSchema.swaggerEndpoint,
                    AppSettings.apiSchema.redocEndpoint,
                    AppSettings.apiSchema.openApiEndpoint,
                )
            )

            // Log the server readiness.
            tracer.withSeverity("Development Mode Enabled: ${environment.developmentMode}.")
            tracer.info("Server configured. Environment: ${AppSettings.runtime.environment}.")
        }
    }
}
