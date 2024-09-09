/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.access.token.routing

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import kcrud.access.credential.CredentialService
import kcrud.access.token.annotation.TokenAPI
import kcrud.access.token.service.AuthenticationTokenService
import kcrud.base.env.SessionContext
import kcrud.base.env.Tracer

/**
 * Application call extension function for responding with a JWT token.
 * Generates a new JWT token for the authenticated session and sends it as a response.
 * To be used only within the Token API.
 *
 * Responds with:
 * - OK (200) and the JWT token if generation is successful.
 * - Bad Request (400) with an error message if the session context is invalid.
 * - Internal Server Error (500) with a general error message if an unexpected error occurs during token generation.
 */
@TokenAPI
internal suspend fun ApplicationCall.respondWithToken() {
    runCatching {
        this.principal<SessionContext>()?.let { sessionContext ->
            return@runCatching AuthenticationTokenService.generate(sessionContext = sessionContext)
        } ?: throw IllegalArgumentException("Invalid actor. ${CredentialService.HINT}")
    }.onSuccess { newJwtToken ->
        this.respond(status = HttpStatusCode.OK, message = newJwtToken)
    }.onFailure { e ->
        Tracer(ref = ApplicationCall::respondWithToken)
            .error(message = "Failed to generate token.", cause = e)

        when (e) {
            is IllegalArgumentException -> {
                this.respond(
                    status = HttpStatusCode.BadRequest,
                    message = "Invalid session context. ${CredentialService.HINT}"
                )
            }

            else -> {
                this.respond(
                    status = HttpStatusCode.InternalServerError,
                    message = "Failed to generate token."
                )
            }
        }
    }
}
