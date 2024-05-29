/*
 * Copyright (c) 2024-Present Perracodex. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.env.health.checks

import kcrud.base.env.EnvironmentType
import kcrud.base.env.health.annotation.HealthCheckAPI
import kcrud.base.settings.AppSettings
import kotlinx.serialization.Serializable

/**
 * Used to check general application's health checks
 * that cannot be categorized by the other concrete health check.
 *
 * @property errors List of errors found during the health check.
 * @property apiSchemaEnabled Whether the API schema generation is enabled.
 */
@HealthCheckAPI
@Serializable
data class ApplicationCheck(
    val errors: MutableList<String>,
    val apiSchemaEnabled: Boolean
) {
    constructor() : this(
        errors = mutableListOf(),
        apiSchemaEnabled = AppSettings.apiSchema.environments.contains(AppSettings.runtime.environment)
    )

    init {
        val environment: EnvironmentType = AppSettings.runtime.environment

        if (environment == EnvironmentType.PROD) {
            if (apiSchemaEnabled) {
                errors.add("${this::class.simpleName}. API schema is enabled in '$environment' environment.")
            }
        }
    }
}
