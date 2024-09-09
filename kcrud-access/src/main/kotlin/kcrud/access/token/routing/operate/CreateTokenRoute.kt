/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.access.token.routing.operate

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.routing.*
import kcrud.access.token.annotation.TokenAPI
import kcrud.access.token.routing.respondWithToken
import kcrud.base.plugins.RateLimitScope
import kcrud.base.settings.AppSettings

/**
 * Generates a new JWT token using Basic Authentication.
 * This endpoint is rate-limited to prevent abuse and requires valid Basic Authentication credentials.
 *
 * See: [Ktor JWT Authentication Documentation](https://ktor.io/docs/server-jwt.html)
 *
 * See: [Basic Authentication Documentation](https://ktor.io/docs/server-basic-auth.html)
 */
@TokenAPI
internal fun Route.createTokenRoute() {
    // Endpoint for initial token generation; requires Basic Authentication credentials.
    rateLimit(configuration = RateLimitName(name = RateLimitScope.NEW_AUTH_TOKEN.key)) {
        authenticate(AppSettings.security.basicAuth.providerName, optional = !AppSettings.security.isEnabled) {
            // Creates a new token and responds with it.
            post("auth/token/create") {
                call.respondWithToken()
            }
        }
    }
}
