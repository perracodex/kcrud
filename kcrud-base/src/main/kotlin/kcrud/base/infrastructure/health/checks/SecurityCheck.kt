/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.infrastructure.health.checks

import kcrud.base.infrastructure.health.annotation.HealthCheckAPI
import kcrud.base.settings.AppSettings
import kotlinx.serialization.Serializable

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
