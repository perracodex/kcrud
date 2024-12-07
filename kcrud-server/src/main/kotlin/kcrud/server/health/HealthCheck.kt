/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.server.health

import io.ktor.server.application.*
import kcrud.core.env.HealthCheckApi
import kcrud.core.settings.AppSettings
import kcrud.core.util.RouteInfo
import kcrud.core.util.collectRoutes
import kcrud.database.service.DatabaseHealth
import kcrud.server.health.check.*
import kotlinx.serialization.Serializable

/**
 * Data class representing the overall health check for the system.
 *
 * @property health List of errors found during any of the health checks.
 * @property application The [ApplicationHealth] check.
 * @property deployment The [DeploymentHealth] check.
 * @property runtime The [RuntimeHealth] check.
 * @property security The [SecurityHealth] check.
 * @property snowflake The [kcrud.server.health.check.SnowflakeHealth] check.
 * @property database The [DatabaseHealth] check.
 * @property endpoints The list of endpoints registered the application.
 */
@OptIn(HealthCheckApi::class)
@Serializable
public data class HealthCheck private constructor(
    val health: MutableList<String>,
    val application: ApplicationHealth,
    val deployment: DeploymentHealth,
    val runtime: RuntimeHealth,
    val security: SecurityHealth,
    val snowflake: SnowflakeHealth,
    val database: DatabaseHealth,
    val endpoints: List<RouteInfo>
) {
    init {
        health.addAll(application.errors)
        health.addAll(deployment.errors)
        health.addAll(runtime.errors)
        health.addAll(security.errors)
        health.addAll(snowflake.errors)
        health.addAll(database.errors)

        if (endpoints.isEmpty()) {
            health.add("No Endpoints Detected.")
        }
        if (health.isEmpty()) {
            health.add("No Errors Detected.")
        }
    }

    internal companion object {
        /**
         * Creates a new [HealthCheck] instance.
         * We need to use a suspendable factory method as some of the checks have suspending functions.
         */
        suspend fun create(call: ApplicationCall): HealthCheck {
            return HealthCheck(
                health = mutableListOf(),
                application = ApplicationHealth(),
                deployment = DeploymentHealth.Companion.create(call = call),
                runtime = RuntimeHealth(call = call, settings = AppSettings.runtime),
                security = SecurityHealth(),
                snowflake = SnowflakeHealth(),
                database = DatabaseHealth.create(settings = AppSettings.database),
                endpoints = call.application.collectRoutes(),
            )
        }
    }
}
