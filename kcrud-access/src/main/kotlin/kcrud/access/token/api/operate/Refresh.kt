/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.access.token.api.operate

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kcrud.access.token.annotation.TokenAPI
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
@TokenAPI
internal fun Route.refreshTokenRoute() {
    /**
     * Refreshes an existing token; requires Basic Authentication credentials.
     * No Basic Authentication is required here, but an existing token's validity will be checked.
     * For example, in Postman set the endpoint and in the Headers add an Authorization key
     * with a 'Bearer' holding a previous valid token.
     * @OpenAPITag Token
     */
    post("auth/token/refresh") {
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
    }
}
