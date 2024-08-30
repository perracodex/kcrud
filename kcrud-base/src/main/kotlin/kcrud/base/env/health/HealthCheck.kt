/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.env.health

import io.ktor.server.application.*
import kcrud.base.database.service.DatabaseService
import kcrud.base.env.health.annotation.HealthCheckAPI
import kcrud.base.env.health.checks.*
import kcrud.base.env.health.utils.collectRoutes
import kotlinx.serialization.Serializable

/**
 * Data class representing the overall health check for the system.
 *
 * @property application The [ApplicationCheck] health check.
 * @property database The [DatabaseCheck] health check.
 * @property deployment The [DeploymentCheck] health check.
 * @property endpoints The list of endpoints detected by the application.
 * @property health List of errors found during any of the health checks.
 * @property runtime The [RuntimeCheck] health check.
 * @property scheduler The [SchedulerCheck] health check.
 * @property security The [SecurityCheck] health check.
 * @property snowflake The [SnowflakeCheck] health check.
 */
@OptIn(HealthCheckAPI::class)
@Serializable
public data class HealthCheck(
    val health: MutableList<String>,
    val application: ApplicationCheck,
    val database: DatabaseCheck,
    val deployment: DeploymentCheck,
    val endpoints: List<String>,
    val runtime: RuntimeCheck,
    val scheduler: SchedulerCheck,
    val security: SecurityCheck,
    val snowflake: SnowflakeCheck
) {
    internal constructor(call: ApplicationCall) : this(
        health = mutableListOf(),
        application = ApplicationCheck(),
        database = DatabaseService.getHealthCheck(),
        deployment = DeploymentCheck(call = call),
        endpoints = call.application.collectRoutes(),
        runtime = RuntimeCheck(call = call),
        scheduler = SchedulerCheck(),
        security = SecurityCheck(),
        snowflake = SnowflakeCheck()
    )

    init {
        health.addAll(application.errors)
        health.addAll(database.errors)
        health.addAll(deployment.errors)
        health.addAll(runtime.errors)
        health.addAll(scheduler.errors)
        health.addAll(security.errors)
        health.addAll(snowflake.errors)

        if (endpoints.isEmpty()) {
            health.add("No Endpoints Detected.")
        }
        if (health.isEmpty()) {
            health.add("No Errors Detected.")
        }
    }
}

