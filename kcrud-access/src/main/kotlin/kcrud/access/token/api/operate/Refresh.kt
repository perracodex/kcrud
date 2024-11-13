/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.access.token.api.operate

import io.github.perracodex.kopapi.dsl.operation.api
import io.github.perracodex.kopapi.dsl.parameter.headerParameter
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kcrud.access.token.annotation.TokenApi
import kcrud.access.token.api.respondWithToken
import kcrud.access.token.service.TokenService

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
    /**
     * Refreshes an existing token; requires Basic Authentication credentials.
     * No Basic Authentication is required here, but an existing token's validity will be checked.
     * For example, in Postman set the endpoint and in the Headers add an Authorization key
     * with a 'Bearer' holding a previous valid token.
     */
    post("/auth/token/refresh") {
        val headers: Headers = call.request.headers

        TokenService.getState(headers = headers).let { result ->
            when (result) {
                is TokenService.TokenState.Valid -> {
                    // Token is still valid; return the same token to the client.
                    call.respondText(
                        text = result.token,
                        status = HttpStatusCode.OK,
                        contentType = ContentType.Text.Plain
                    )
                }

                is TokenService.TokenState.Expired -> {
                    // Token has expired; generate a new token and respond with it.
                    call.respondWithToken()
                }

                is TokenService.TokenState.Invalid -> {
                    // Token is invalid; respond with an Unauthorized status.
                    call.respond(status = HttpStatusCode.Unauthorized, message = "Invalid token.")
                }
            }
        }
    } api {
        tags = setOf("Token")
        summary = "Refresh an existing JWT token."
        description = "Allows a client to refresh their existing JWT token."
        operationId = "refreshToken"
        headerParameter<String>(name = HttpHeaders.AuthenticationInfo) {
            description = "The JWT token to be refreshed."
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
