/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.server.health.check

import io.ktor.server.application.*
import kcrud.core.env.EnvironmentType
import kcrud.core.env.HealthCheckApi
import kcrud.core.settings.AppSettings
import kcrud.core.util.DateTimeUtils.current
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
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
@HealthCheckApi
@Serializable
public data class RuntimeHealth(
    val errors: MutableList<String>,
    val machineId: Int,
    val environment: EnvironmentType,
    val developmentModeEnabled: Boolean,
    val utc: Instant,
    val local: LocalDateTime,
) {
    internal constructor(call: ApplicationCall) : this(
        errors = mutableListOf(),
        machineId = AppSettings.runtime.machineId,
        environment = AppSettings.runtime.environment,
        developmentModeEnabled = call.application.developmentMode,
        utc = Instant.current(),
        local = LocalDateTime.current()
    )

    init {
        val className: String? = this::class.simpleName

        if (machineId == 0) {
            errors.add("$className. Machine ID is not set.")
        }

        if (environment == EnvironmentType.PROD && developmentModeEnabled) {
            errors.add("$className. Development mode flag enabled in $environment.")
        }
    }
}
