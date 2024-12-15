/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package krud.access.domain.token.api.operate

import io.github.perracodex.kopapi.dsl.operation.api
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import krud.access.domain.token.annotation.TokenApi
import krud.access.domain.token.service.TokenService
import krud.base.context.SessionContext
import krud.base.context.getContext
import krud.base.plugins.RateLimitScope
import krud.base.settings.AppSettings

/**
 * Generates a new JWT token using Basic Authentication.
 * This endpoint is rate-limited to prevent abuse and requires valid Basic Authentication credentials.
 *
 * #### References
 * - [Ktor JWT Authentication](https://ktor.io/docs/server-jwt.html)
 * - [Basic Authentication](https://ktor.io/docs/server-basic-auth.html)
 */
@TokenApi
internal fun Route.createTokenRoute() {
    rateLimit(configuration = RateLimitName(name = RateLimitScope.NEW_AUTH_TOKEN.key)) {
        authenticate(AppSettings.security.basicAuth.providerName, optional = !AppSettings.security.isEnabled) {
            post("/auth/token/create") {
                val sessionContext: SessionContext = call.getContext()

                TokenService.createToken(sessionContext = sessionContext).let { response ->
                    call.respondText(
                        text = response.message,
                        status = response.statusCode,
                        contentType = ContentType.Text.Plain
                    )
                }
            } api {
                tags = setOf("Token")
                summary = "Create a new JWT token."
                description = "Generates a new JWT token using Basic Authentication."
                operationId = "createToken"
                basicSecurity(name = "TokenCreation") {
                    description = "Generates a new JWT token using Basic Authentication."
                }
                response<String>(status = HttpStatusCode.OK) {
                    description = "The generated JWT token."
                    contentType = setOf(ContentType.Text.Plain)
                }
                response(status = HttpStatusCode.Unauthorized) {
                    description = "No valid credentials provided."
                }
                response(status = HttpStatusCode.InternalServerError) {
                    description = "Failed to generate token."
                }
            }
        }
    }
}
