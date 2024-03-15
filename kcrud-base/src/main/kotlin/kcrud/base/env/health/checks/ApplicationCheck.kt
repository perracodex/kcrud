/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.env.health.checks

import kcrud.base.env.EnvironmentType
import kcrud.base.env.health.annotation.HealthCheckAPI
import kcrud.base.settings.AppSettings
import kotlinx.serialization.Serializable

@HealthCheckAPI
@Serializable
data class ApplicationCheck(
    val errors: MutableList<String>,
    val docsEnabled: Boolean
) {
    constructor() : this(
        errors = mutableListOf(),
        docsEnabled = AppSettings.docs.environments.contains(AppSettings.runtime.environment)
    )

    init {
        val environment: EnvironmentType = AppSettings.runtime.environment

        if (environment == EnvironmentType.PROD) {
            if (docsEnabled) {
                errors.add("${this::class.simpleName}. Docs are enabled in '$environment' environment.")
            }
        }
    }
}
