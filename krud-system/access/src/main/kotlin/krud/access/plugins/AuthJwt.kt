/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package krud.access.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import krud.access.context.SessionContextFactory
import krud.access.domain.token.annotation.TokenApi
import krud.core.context.clearContext
import krud.core.context.setContext
import krud.core.env.Tracer
import krud.core.settings.AppSettings

/**
 * Configures JWT-based authentication.
 *
 * JSON Web Token (JWT) is an open standard that defines a way for securely transmitting information
 * between parties as a JSON object. This information can be verified and trusted since it is signed
 * using a shared secret (with the HS256 algorithm) or a public/private key pair (for example, RS256).
 *
 * Ktor handles JWTs passed in the Authorization header using the Bearer schema and allows to:
 * 1. Verify the signature of a JSON web token.
 * 2. Perform additional validations on the JWT payload.
 *
 * #### References
 * - [Ktor JWT Authentication Documentation](https://ktor.io/docs/server-jwt.html)
 */
@OptIn(TokenApi::class)
public fun Application.configureJwtAuthentication() {

    authentication {
        jwt(name = AppSettings.security.jwtAuth.providerName) {
            realm = AppSettings.security.jwtAuth.realm

            verifier(
                // Configure the JWT verifier used to check the signature of each incoming JWT token.
                // The signature check ensures that the token was signed with the same secret key
                // and thus can be trusted as being issued by the application.
                JWT.require(Algorithm.HMAC256(AppSettings.security.jwtAuth.secretKey))
                    .withAudience(AppSettings.security.jwtAuth.audience)
                    .withIssuer(AppSettings.security.jwtAuth.issuer)
                    .build()
            )

            // Block to validate the JWT token.
            // The JWT library automatically verifies the token's signature before this block.
            // This ensures that the token was not tampered with and was signed with the correct secret key.
            validate { credential ->
                SessionContextFactory.from(
                    headers = this.request.headers,
                    jwtCredential = credential
                )?.let { sessionContext ->
                    return@validate this.setContext(sessionContext = sessionContext)
                }

                this.clearContext()
                return@validate null
            }

            challenge { defaultScheme, realm ->
                Tracer(ref = Application::configureJwtAuthentication).error(
                    "JWT authentication failed. Default scheme: $defaultScheme, realm: $realm"
                )
                call.clearContext()
                call.respond(status = HttpStatusCode.Unauthorized, message = "Token is not valid or has expired.")
            }
        }
    }
}
