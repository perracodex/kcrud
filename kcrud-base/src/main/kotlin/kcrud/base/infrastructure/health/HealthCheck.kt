/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.infrastructure.health

import io.ktor.server.application.*
import kcrud.base.database.service.DatabaseService
import kcrud.base.infrastructure.health.annotation.HealthCheckAPI
import kcrud.base.infrastructure.health.checks.*
import kcrud.base.infrastructure.health.utils.collectRoutes
import kotlinx.serialization.Serializable

/**
 * Data class representing the health check for the system.
 */
@OptIn(HealthCheckAPI::class)
@Serializable
data class HealthCheck(
    val health: MutableList<String>,
    val runtime: RuntimeCheck,
    val deployment: DeploymentCheck,
    val security: SecurityCheck,
    val database: DatabaseCheck,
    val application: ApplicationCheck,
    val snowflake: SnowflakeCheck,
    val endpoints: List<String>
) {
    constructor(call: ApplicationCall?) : this(
        health = mutableListOf(),
        runtime = RuntimeCheck(call = call),
        deployment = DeploymentCheck(call = call),
        security = SecurityCheck(),
        database = DatabaseService.getHealthCheck(),
        application = ApplicationCheck(),
        snowflake = SnowflakeCheck(),
        endpoints = call?.application?.collectRoutes() ?: emptyList()
    )

    init {
        health.addAll(runtime.errors)
        health.addAll(deployment.errors)
        health.addAll(security.errors)
        health.addAll(database.errors)
        health.addAll(application.errors)
        health.addAll(snowflake.errors)

        if (endpoints.isEmpty()) {
            health.add("No Endpoints Detected.")
        }

        if (health.isEmpty()) {
            health.add("No Errors Detected.")
        }
    }
}
