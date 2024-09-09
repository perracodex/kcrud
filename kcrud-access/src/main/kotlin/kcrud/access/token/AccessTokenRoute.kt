/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.access.token

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kcrud.access.credential.CredentialService
import kcrud.base.env.SessionContext
import kcrud.base.env.Tracer
import kcrud.base.plugins.RateLimitScope
import kcrud.base.settings.AppSettings

/**
 * Defines routes for handling access tokens within the authentication system,
 * using JWT and Basic Authentication as specified in Ktor's documentation.
 *
 * • `create`: Generates a new JWT token using Basic Authentication.
 *   This endpoint is rate-limited to prevent abuse and requires valid Basic Authentication credentials.
 *
 * • `refresh`: Allows a client to refresh their existing JWT token. This endpoint does not require
 *   Basic Authentication but does require a valid JWT token in the 'Authorization' header.
 *   Depending on the state of the provided token (valid, expired, or invalid), it either returns
 *   the same token, generates a new one, or denies access.
 *
 * See: [Ktor JWT Authentication Documentation](https://ktor.io/docs/server-jwt.html)
 *
 * See: [Basic Authentication Documentation](https://ktor.io/docs/server-basic-auth.html)
 */
public fun Route.accessTokenRoute() {

    route("auth/token") {

        // Endpoint for initial token generation; requires Basic Authentication credentials.
        rateLimit(configuration = RateLimitName(name = RateLimitScope.NEW_AUTH_TOKEN.key)) {
            authenticate(AppSettings.security.basicAuth.providerName, optional = !AppSettings.security.isEnabled) {
                // Creates a new token and responds with it.
                post("create") {
                    call.respondWithToken()
                }
            }
        }

        // Endpoint for token refresh.
        // No Basic Authentication is required here, but an existing token's validity will be checked.
        // For example, in Postman set the endpoint and in the Headers add an Authorization key
        // with a 'Bearer' holding a previous valid token.
        post("refresh") {
            val headers: Headers = call.request.headers
            val tokenState: AuthenticationTokenService.TokenState = AuthenticationTokenService.getState(headers = headers)

            when (tokenState) {
                AuthenticationTokenService.TokenState.VALID -> {
                    // Token is still valid; return the same token to the client.
                    val jwtToken: String = AuthenticationTokenService.fromHeader(headers = call.request.headers)
                    call.respond(status = HttpStatusCode.OK, message = jwtToken)
                }

                AuthenticationTokenService.TokenState.EXPIRED -> {
                    // Token has expired; generate a new token and respond with it.
                    call.respondWithToken()
                }

                AuthenticationTokenService.TokenState.INVALID -> {
                    // Token is invalid; respond with an Unauthorized status.
                    call.respond(status = HttpStatusCode.Unauthorized, message = "Invalid token.")
                }
            }
        }
    }
}

/**
 * Generates a new JWT token for the authenticated session and sends it as a response.
 *
 * Responds with:
 * - OK (200) and the JWT token if generation is successful.
 * - Bad Request (400) with an error message if the session context is invalid.
 * - Internal Server Error (500) with a general error message if an unexpected error occurs during token generation.
 */
private suspend fun ApplicationCall.respondWithToken() {
    val tracer = Tracer(ref = ApplicationCall::respondWithToken)

    try {
        val sessionContext: SessionContext = this.principal<SessionContext>()
            ?: throw IllegalArgumentException("Invalid actor. ${CredentialService.HINT}")

        val newJwtToken: String = AuthenticationTokenService.generate(sessionContext = sessionContext)
        respond(status = HttpStatusCode.OK, message = newJwtToken)
    } catch (e: IllegalArgumentException) {
        tracer.error(message = "Failed to generate token due to invalid session context.", cause = e)
        respond(
            status = HttpStatusCode.BadRequest,
            message = e.message ?: "Invalid session context. ${CredentialService.HINT}"
        )
    } catch (e: Exception) {
        tracer.error(message = "Failed to generate token due to an unexpected error.", cause = e)
        respond(status = HttpStatusCode.InternalServerError, message = "Failed to generate token.")
    }
}
