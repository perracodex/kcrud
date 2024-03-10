/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.settings.config.sections.security.sections

import kcrud.base.settings.config.parser.IConfigSection

/**
 * Security constraints settings.
 *
 * @property publicApi Rate limit specification for the Public API endpoints.
 * @property newToken Rate limit specification for the New Authentication Token generation endpoint.
 */
data class ConstraintsSettings(
    val publicApi: LimitSpec,
    val newToken: LimitSpec
) : IConfigSection {
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
