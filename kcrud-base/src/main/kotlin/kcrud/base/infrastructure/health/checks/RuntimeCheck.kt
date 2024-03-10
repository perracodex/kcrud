/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.infrastructure.health.checks

import io.ktor.server.application.*
import kcrud.base.infrastructure.env.EnvironmentType
import kcrud.base.infrastructure.health.annotation.HealthCheckAPI
import kcrud.base.infrastructure.utils.DateTimeUtils
import kcrud.base.infrastructure.utils.KLocalDateTime
import kcrud.base.settings.AppSettings
import kotlinx.serialization.Serializable

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
        val timestamp: KLocalDateTime = DateTimeUtils.currentUTCDateTime()
    }
}
