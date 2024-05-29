/*
 * Copyright (c) 2024-Present Perracodex. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.env.health.checks

import kcrud.base.env.health.annotation.HealthCheckAPI
import kcrud.base.settings.AppSettings
import kotlinx.serialization.Serializable

/**
 * Used to check the security configuration of the application.
 *
 * @property errors List of errors found during the health check.
 * @property isEnabled Flag indicating if security (JWT, Basic, etc.) is enabled, if not, the application is not secure.
 * @property useSecureConnection Flag indicating if secure connections are used.
 * @property publicApiRateLimit Rate limit specification for the public API endpoints.
 * @property publicApiRateRefillMs The rate refill time for public API endpoints, in milliseconds
 * @property newTokenRateLimit Rate limit specification for the New Authentication Token generation endpoint.
 * @property newTokenRateRefillMs The rate refill time for the New Authentication Token generation endpoint, in milliseconds.
 */
@HealthCheckAPI
@Serializable
data class SecurityCheck(
    val errors: MutableList<String>,
    val isEnabled: Boolean,
    val useSecureConnection: Boolean,
    val publicApiRateLimit: Int,
    val publicApiRateRefillMs: Long,
    val newTokenRateLimit: Int,
    val newTokenRateRefillMs: Long,
) {
    constructor() : this(
        errors = mutableListOf(),
        isEnabled = AppSettings.security.isEnabled,
        useSecureConnection = AppSettings.security.useSecureConnection,
        publicApiRateLimit = AppSettings.security.constraints.publicApi.limit,
        publicApiRateRefillMs = AppSettings.security.constraints.publicApi.refillMs,
        newTokenRateLimit = AppSettings.security.constraints.newToken.limit,
        newTokenRateRefillMs = AppSettings.security.constraints.newToken.refillMs
    ) {
        if (!isEnabled) {
            errors.add("${this::class.simpleName}. Security is disabled.")
        }
    }
}
