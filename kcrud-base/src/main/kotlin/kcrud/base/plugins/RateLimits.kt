/*
 * Copyright (c) 2023-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.ratelimit.*
import kcrud.base.settings.AppSettings
import kotlin.time.Duration.Companion.milliseconds

/**
 * Configures the routes [RateLimit] plugin, which defines the maximum allowed calls per period.
 *
 * Rate limits must be applied when defining the routes by using the same scope key. Example:
 *
 * The [RateLimit] plugin allows to limit the number of requests a client can make within
 * a certain time period. Ktor provides different means for configuring rate limiting.
 * For example, enable rate limiting globally for a whole application or configure
 * different rate limits for different resources. Also, to configure rate limiting based
 * on specific request parameters: an IP address, an API key or access token, and so on.
 *
 *```
 * routing {
 *      rateLimit(RateLimitName(RateLimitScope.PUBLIC_API)) {
 *           get("some_endpoint") { ... }
 *      }
 * }
 *```
 *
 * See: [Ktor Rate Limit](https://ktor.io/docs/rate-limit.html)
 */
fun Application.configureRateLimit() {

    install(plugin = RateLimit) {
        // Example scope for the public API rate limit.
        register(RateLimitName(name = RateLimitScope.PUBLIC_API.key)) {
            rateLimiter(
                limit = AppSettings.security.constraints.publicApi.limit,
                refillPeriod = AppSettings.security.constraints.publicApi.refillMs.milliseconds
            )
        }

        // Example scope for new token generation rate limit.
        register(RateLimitName(name = RateLimitScope.NEW_AUTH_TOKEN.key)) {
            rateLimiter(
                limit = AppSettings.security.constraints.newToken.limit,
                refillPeriod = AppSettings.security.constraints.newToken.refillMs.milliseconds
            )
        }
    }
}

/**
 * The rate limit scopes used in the application.
 */
enum class RateLimitScope(val key: String) {
    NEW_AUTH_TOKEN(key = "new_auth_token"), // Scope key for authorization tokens.
    PUBLIC_API(key = "public_api") // Scope key for the public API.
}
