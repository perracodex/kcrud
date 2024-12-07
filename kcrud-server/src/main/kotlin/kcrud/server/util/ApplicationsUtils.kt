/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.server.util

import io.ktor.server.application.*
import kcrud.access.domain.actor.service.ActorSyncService
import kcrud.core.env.Tracer
import kcrud.core.security.snowflake.SnowflakeFactory
import kcrud.core.settings.AppSettings
import kcrud.core.util.NetworkUtils
import kotlinx.coroutines.launch

/**
 * Utility functions for the application server.
 */
internal object ApplicationsUtils {
    private val tracer: Tracer = Tracer<ApplicationsUtils>()

    /**
     * Perform any additional server configuration that is required for the application to run.
     *
     * @param application The Ktor application instance.
     */
    fun completeServerConfiguration(application: Application) {
        // Add a hook to refresh the Credentials and RBAC services when the application starts.
        application.monitor.subscribe(definition = ApplicationStarted) {
            application.launch {
                ActorSyncService.refresh()
            }
        }

        // Watch the server for readiness.
        application.monitor.subscribe(definition = ServerReady) {
            outputState(application = application)
        }
    }

    /**
     * Output the server state to the console, including main endpoints and configuration.
     *
     * @param application The Ktor application instance.
     */
    private fun outputState(application: Application) {
        // Dumps the server's endpoints to the console for easy access and testing.
        // This does not include the actual API routes endpoints.
        NetworkUtils.logEndpoints(reason = "Demo", endpoints = listOf("demo"))
        NetworkUtils.logEndpoints(reason = "Healthcheck", endpoints = listOf("admin/health"))
        NetworkUtils.logEndpoints(reason = "Snowflake", endpoints = listOf("admin/snowflake/${SnowflakeFactory.nextId()}"))
        NetworkUtils.logEndpoints(reason = "Micrometer Metrics", endpoints = listOf("admin/metrics"))

        if (AppSettings.security.rbac.isEnabled) {
            NetworkUtils.logEndpoints(reason = "RBAC", endpoints = listOf("rbac/login"))
        }

        // Log the server readiness.
        tracer.withSeverity("Development Mode Enabled: ${application.developmentMode}")
        tracer.info("Server configured. Environment: ${AppSettings.runtime.environment}")
    }
}
