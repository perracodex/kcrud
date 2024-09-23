/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.env.health.checks

import io.ktor.server.application.*
import kcrud.base.env.EnvironmentType
import kcrud.base.env.health.annotation.HealthCheckAPI
import kcrud.base.settings.AppSettings
import kcrud.base.utils.DateTimeUtils
import kcrud.base.utils.KLocalDateTime
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Used to check the runtime configuration of the application.
 *
 * @property errors List of errors found during the health check.
 * @property machineId The unique identifier of the machine running the application.
 * @property environment The [EnvironmentType] the application is running in.
 * @property developmentModeEnabled Flag indicating if development mode is enabled.
 * @property utc The current UTC timestamp.
 * @property local The current local timestamp.
 */
@HealthCheckAPI
@Serializable
public data class RuntimeCheck(
    val errors: MutableList<String>,
    val machineId: Int,
    val environment: EnvironmentType,
    val developmentModeEnabled: Boolean,
    val utc: Instant,
    val local: KLocalDateTime,
) {
    internal constructor(call: ApplicationCall) : this(
        errors = mutableListOf(),
        machineId = AppSettings.runtime.machineId,
        environment = AppSettings.runtime.environment,
        developmentModeEnabled = call.application.developmentMode,
        utc = DateTimeUtils.utcDateTime(),
        local = DateTimeUtils.currentDateTime()
    )

    init {
        val className: String? = this::class.simpleName

        if (machineId == 0) {
            errors.add("$className. Machine ID is not set.")
        }

        if (environment == EnvironmentType.PROD && developmentModeEnabled) {
            errors.add("$className. Development mode flag enabled in ${environment}.")
        }
    }
}
