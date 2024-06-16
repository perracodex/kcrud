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
import kcrud.base.plugins.RateLimitScope
import kcrud.base.settings.AppSettings

/**
 * Access-token endpoints.
 *
 * See: [Ktor JWT Authentication Documentation](https://ktor.io/docs/server-jwt.html)
 *
 * See: [Basic Authentication Documentation](https://ktor.io/docs/server-basic-auth.html)
 */
fun Route.accessTokenRoute() {

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
            val tokenState: AuthenticationTokenService.TokenState = AuthenticationTokenService.getState(call = call)

            when (tokenState) {
                AuthenticationTokenService.TokenState.VALID -> {
                    // Token is still valid; return the same token to the client.
                    val jwtToken: String = AuthenticationTokenService.fromHeader(call = call)
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
 * Generates a new JWT token and sends it as a response.
 * If token generation fails, responds with an Internal Server Error status.
 */
private suspend fun ApplicationCall.respondWithToken() {
    try {
        val sessionContext: SessionContext = this.principal<SessionContext>()
            ?: throw IllegalArgumentException("Invalid actor. ${CredentialService.HINT}")
        val newJwtToken: String = AuthenticationTokenService.generate(sessionContext = sessionContext)
        respond(status = HttpStatusCode.OK, message = newJwtToken)
    } catch (e: IllegalArgumentException) {
        respond(status = HttpStatusCode.BadRequest, message = e.message ?: "Invalid actor. ${CredentialService.HINT}")
    } catch (e: Exception) {
        respond(status = HttpStatusCode.InternalServerError, message = "Failed to generate token.")
    }
}
