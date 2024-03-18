/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.access.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import kcrud.access.system.SessionContextFactory
import kcrud.base.env.SessionContext
import kcrud.base.settings.AppSettings

/**
 * Configures JWT-based authentication.
 *
 * JSON Web Token (JWT) is an open standard that defines a way for securely transmitting information
 * between parties as a JSON object. This information can be verified and trusted since it is signed
 * using a shared secret (with the HS256 algorithm) or a public/private key pair (for example, RS256).
 *
 * Ktor handles JWTs passed in the Authorization header using the Bearer schema and allows to:
 *
 * 1. Verify the signature of a JSON web token.
 * 2. Perform additional validations on the JWT payload.
 *
 *
 * See: [Ktor JWT Authentication Documentation](https://ktor.io/docs/jwt.html)
 */
fun Application.configureJwtAuthentication() {

    authentication {
        jwt(name = AppSettings.security.jwt.providerName) {
            realm = AppSettings.security.jwt.realm

            verifier(
                // Configure the JWT verifier used to check the signature of each incoming JWT token.
                // The signature check ensures that the token was signed with the same secret key
                // and thus can be trusted as being issued by your application.
                JWT.require(Algorithm.HMAC256(AppSettings.security.jwt.secretKey))
                    .withAudience(AppSettings.security.jwt.audience)
                    .withIssuer(AppSettings.security.jwt.issuer)
                    .build()
            )

            // Block to validate the JWT token.
            // The JWT library automatically verifies the token's signature before this block.
            // This ensures that the token was not tampered with and was signed with the correct secret key.
            validate { credential ->
                SessionContextFactory.from(jwtCredential = credential)?.let { sessionContext ->
                    this.sessions.set(name = SessionContext.SESSION_NAME, value = sessionContext)
                    return@validate sessionContext
                }

                this.sessions.clear(name = SessionContext.SESSION_NAME)
                null
            }

            challenge { _, _ ->
                call.sessions.clear(name = SessionContext.SESSION_NAME)
                call.respond(status = HttpStatusCode.Unauthorized, message = "Token is not valid or has expired.")
            }
        }
    }
}
