/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.server.util

import io.ktor.server.application.*
import kcrud.core.env.Tracer
import kcrud.core.security.snowflake.SnowflakeFactory
import kcrud.core.settings.AppSettings
import kcrud.core.util.NetworkUtils

/**
 * Utility functions for the application server.
 */
internal object ApplicationsUtils {
    private val tracer = Tracer<ApplicationsUtils>()

    /**
     * Watches the server for readiness and logs the server's endpoints to the console.
     */
    fun watchServer(application: Application) {
        application.monitor.subscribe(definition = ServerReady) {

            // Dumps the server's endpoints to the console for easy access and testing.
            // This does not include the actual API routes endpoints.
            NetworkUtils.logEndpoints(reason = "Demo", endpoints = listOf("demo"))
            NetworkUtils.logEndpoints(reason = "Healthcheck", endpoints = listOf("admin/health"))
            NetworkUtils.logEndpoints(reason = "Snowflake", endpoints = listOf("admin/snowflake/${SnowflakeFactory.nextId()}"))
            NetworkUtils.logEndpoints(reason = "Scheduler", endpoints = listOf("admin/scheduler/dashboard"))
            NetworkUtils.logEndpoints(reason = "Micrometer Metrics", endpoints = listOf("admin/metrics"))

            if (AppSettings.security.rbac.isEnabled) {
                NetworkUtils.logEndpoints(reason = "RBAC", endpoints = listOf("rbac/login"))
            }

            // Log the server readiness.
            tracer.withSeverity("Development Mode Enabled: ${application.developmentMode}")
            tracer.info("Server configured. Environment: ${AppSettings.runtime.environment}")
        }
    }
}
