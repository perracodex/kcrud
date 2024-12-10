/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package krud.access.domain.token.api.operate

import io.github.perracodex.kopapi.dsl.operation.api
import io.github.perracodex.kopapi.dsl.parameter.headerParameter
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import krud.access.domain.token.annotation.TokenApi
import krud.access.domain.token.service.TokenService

/**
 * Allows a client to refresh their existing JWT token. This endpoint does not require
 * Basic Authentication but does require a valid JWT token in the 'Authorization' header.
 * Depending on the state of the provided token (valid, expired, or invalid), it either
 * returns the same token, generates a new one, or denies access.
 *
 * #### References
 * - [Ktor JWT Authentication](https://ktor.io/docs/server-jwt.html)
 * - [Basic Authentication](https://ktor.io/docs/server-basic-auth.html)
 */
@TokenApi
internal fun Route.refreshTokenRoute() {
    post("/auth/token/refresh") {
        val headers: Headers = call.request.headers

        TokenService.refreshToken(headers = headers).let { response ->
            call.respondText(
                text = response.message,
                status = response.statusCode,
                contentType = ContentType.Text.Plain
            )
        }
    } api {
        tags = setOf("Token")
        summary = "Refresh an existing JWT token."
        description = "Allows a client to refresh their existing JWT token."
        operationId = "refreshToken"
        headerParameter<String>(name = HttpHeaders.AuthenticationInfo) {
            description = "The JWT token to be refreshed."

            example {
                value = "Bearer SOME_JWT_TOKEN"
            }
        }
        response<String>(status = HttpStatusCode.OK) {
            description = "The refreshed JWT token."
            contentType = setOf(ContentType.Text.Plain)
        }
        response(status = HttpStatusCode.Unauthorized) {
            description = "Invalid token."
        }
        response(status = HttpStatusCode.InternalServerError) {
            description = "Failed to refresh token."
        }
        noSecurity()
    }
}
