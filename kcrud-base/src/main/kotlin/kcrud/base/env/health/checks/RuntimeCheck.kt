/*
 * Copyright (c) 2024-Present Perracodex. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.env.health.checks

import io.ktor.server.application.*
import kcrud.base.env.EnvironmentType
import kcrud.base.env.health.annotation.HealthCheckAPI
import kcrud.base.settings.AppSettings
import kcrud.base.utils.DateTimeUtils
import kcrud.base.utils.KLocalDateTime
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
data class RuntimeCheck(
    val errors: MutableList<String>,
    val machineId: Int,
    val environment: EnvironmentType,
    val developmentModeEnabled: Boolean,
    val utc: KLocalDateTime,
    val local: KLocalDateTime,
) {
    constructor(call: ApplicationCall?) : this(
        errors = mutableListOf(),
        machineId = AppSettings.runtime.machineId,
        environment = AppSettings.runtime.environment,
        developmentModeEnabled = call?.application?.developmentMode ?: false,
        utc = timestamp,
        local = DateTimeUtils.utcToLocal(utc = timestamp),
    )

    init {
        val className: String? = this::class.simpleName

        if (machineId == 0) {
            errors.add("$className. Machine ID is not set.")
        }

        if (environment == EnvironmentType.PROD && developmentModeEnabled) {
            errors.add("$className. Development mode flag enabled in ${environment}.")
        }

        val utcToLocal: KLocalDateTime = DateTimeUtils.utcToLocal(utc = utc)
        if (utcToLocal != local) {
            errors.add("$className. Runtime UTC and Local mismatch. UTC: $utc, Local: $local, UTC to Local: $utcToLocal.")
        }
    }

    companion object {
        /** The current UTC timestamp. */
        private val timestamp: KLocalDateTime = DateTimeUtils.currentUTCDateTime()
    }
}
