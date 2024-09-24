/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.core.env.health.checks

import kcrud.core.env.health.annotation.HealthCheckAPI
import kcrud.core.settings.AppSettings
import kcrud.core.settings.config.sections.security.sections.ConstraintsSettings.LimitSpec
import kotlinx.serialization.Serializable

/**
 * Used to check the security configuration of the application.
 *
 * @property errors List of errors found during the health check.
 * @property isEnabled Flag indicating if security (JWT, Basic, etc.) is enabled, if not, the application is not secure.
 * @property useSecureConnection Flag indicating if secure connections are used.
 * @property publicApi The rate limit specification for public API endpoints.
 * @property privateApi The rate limit specification for private API endpoints.
 * @property newToken The rate limit specification for the new authentication token generation endpoint.
 */
@HealthCheckAPI
@Serializable
public data class SecurityCheck(
    val errors: MutableList<String>,
    val isEnabled: Boolean,
    val useSecureConnection: Boolean,
    val publicApi: LimitSpec,
    val privateApi: LimitSpec,
    val newToken: LimitSpec,
) {
    internal constructor() : this(
        errors = mutableListOf(),
        isEnabled = AppSettings.security.isEnabled,
        useSecureConnection = AppSettings.security.useSecureConnection,
        publicApi = AppSettings.security.constraints.publicApi,
        privateApi = AppSettings.security.constraints.privateApi,
        newToken = AppSettings.security.constraints.newToken
    ) {
        if (!isEnabled) {
            errors.add("${this::class.simpleName}. Security is disabled.")
        }
    }
}
