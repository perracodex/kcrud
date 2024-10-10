/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.access.token.api.operate

import io.ktor.server.auth.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.routing.*
import kcrud.access.token.annotation.TokenAPI
import kcrud.access.token.api.respondWithToken
import kcrud.core.plugins.RateLimitScope
import kcrud.core.settings.AppSettings

/**
 * Generates a new JWT token using Basic Authentication.
 * This endpoint is rate-limited to prevent abuse and requires valid Basic Authentication credentials.
 *
 * #### References
 * - [Ktor JWT Authentication](https://ktor.io/docs/server-jwt.html)
 * - [Basic Authentication](https://ktor.io/docs/server-basic-auth.html)
 */
@TokenAPI
internal fun Route.createTokenRoute() {
    rateLimit(configuration = RateLimitName(name = RateLimitScope.NEW_AUTH_TOKEN.key)) {
        authenticate(AppSettings.security.basicAuth.providerName, optional = !AppSettings.security.isEnabled) {
            /**
             * Creates a new token; requires Basic Authentication credentials.
             * @OpenAPITag Token
             */
            post("auth/token/create") {
                call.respondWithToken()
            }
        }
    }
}
