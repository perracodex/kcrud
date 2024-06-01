/*
 * Copyright (c) 2024-Present Perracodex. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.settings.config.sections.security.sections

import kcrud.base.settings.config.parser.IConfigSection
import kotlinx.serialization.Serializable

/**
 * Security constraints settings.
 *
 * @property publicApi Rate limit specification for the Public API endpoints.
 * @property newToken Rate limit specification for the New Authentication Token generation endpoint.
 */
@Serializable
data class ConstraintsSettings(
    val publicApi: LimitSpec,
    val newToken: LimitSpec
) : IConfigSection {

    /**
     * Rate limit specification.
     * For example, a limit of 10 requests per second would be represented as:
     * ```
     * LimitSpec(limit = 10, refillMs = 1000)
     * ```
     *
     * @property limit The maximum number of requests allowed within the refill period. Must be > 0.
     * @property refillMs The time period in milliseconds after which the limit is reset. Must be > 0.
     */
    @Serializable
    data class LimitSpec(
        val limit: Int,
        val refillMs: Long
    ) {
        init {
            require(limit > 0) { "Invalid rate limit. Must be > 0." }
            require(refillMs > 0L) { "Invalid rate refill. Must be > 0." }
        }
    }
}
