/*
 * Copyright (c) 2023-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import kcrud.base.infrastructure.env.SessionContext
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
                // Check if the JWT audience claim matches the configured audience.
                // This ensures the token is intended for the application.
                if (!credential.payload.audience.contains(AppSettings.security.jwt.audience)) {
                    return@validate null
                }

                // Check if the JWT issuer matches the configured issuer.
                // This ensures the token was issued by a trusted source.
                if (credential.payload.issuer != AppSettings.security.jwt.issuer) {
                    return@validate null
                }

                // If both claims are valid, create and return a SessionContext from the JWT payload,
                // so it can be accessed from any call's principal.
                // If not, return null to indicate the token is invalid or not applicable.
                val sessionContext: SessionContext? = SessionContext.from(jwtPayload = credential.payload)
                sessionContext
            }

            challenge { _, _ ->
                call.respond(status = HttpStatusCode.Unauthorized, message = "Token is not valid or has expired.")
            }
        }
    }
}
