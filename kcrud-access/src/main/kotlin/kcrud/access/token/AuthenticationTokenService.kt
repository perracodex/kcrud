/*
 * Copyright (c) 2024-Present Perracodex. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.access.token

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTDecodeException
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import io.ktor.http.*
import io.ktor.http.auth.*
import io.ktor.server.application.*
import kcrud.base.env.SessionContext
import kcrud.base.env.Tracer
import kcrud.base.settings.AppSettings
import kcrud.base.settings.config.sections.security.sections.JwtSettings
import kotlinx.serialization.json.Json
import java.util.*
import kotlin.time.Duration.Companion.seconds

/**
 * Security class responsible for verifying JWT tokens.
 *
 * This class contains the logic for verifying JWT tokens using HMAC256 algorithm.
 * If the token is invalid or any exception occurs, an UnauthorizedException will be thrown.
 */
object AuthenticationTokenService {
    private val tracer = Tracer<AuthenticationTokenService>()

    /**
     * The authentication token state.
     */
    enum class TokenState {
        /** The token is valid. */
        VALID,

        /** The token has expired. */
        EXPIRED,

        /** The token is invalid. */
        INVALID
    }

    /**
     * Returns the current [TokenState] from the header authorization token.
     */
    fun getState(call: ApplicationCall): TokenState {
        return try {
            val token: String = fromHeader(call = call)
            val algorithm: Algorithm = Algorithm.HMAC256(AppSettings.security.jwt.secretKey)
            val verifier: JWTVerifier = JWT.require(algorithm).build()
            val decodedToken = JWT.decode(token)
            verifier.verify(decodedToken)
            TokenState.VALID
        } catch (e: TokenExpiredException) {
            TokenState.EXPIRED
        } catch (e: JWTDecodeException) {
            tracer.error("Failed to decide token: ${e.message}")
            TokenState.INVALID
        } catch (e: JWTVerificationException) {
            tracer.error("Token verification failed: ${e.message}")
            TokenState.INVALID
        } catch (e: IllegalArgumentException) {
            tracer.error("Unexpected problem verifying token: ${e.message}")
            TokenState.INVALID
        }
    }

    /**
     * Returns the current authorization token from the headers.
     */
    fun fromHeader(call: ApplicationCall): String {
        val authHeader: String? = call.request.headers.entries().find {
            it.key.equals(other = HttpHeaders.Authorization, ignoreCase = true)
        }?.value?.get(index = 0)

        if (authHeader.isNullOrBlank() || !authHeader.startsWith(prefix = AuthScheme.Bearer, ignoreCase = true)) {
            throw IllegalArgumentException("Invalid Authorization header format.")
        }

        return authHeader.substring(AuthScheme.Bearer.length).trim()
    }

    /**
     * Generate a new authorization token.
     *
     * @param sessionContext The [SessionContext] details to embed in the token.
     * @return The generated JWT token.
     */
    fun generate(sessionContext: SessionContext): String {
        val jwtSettings: JwtSettings = AppSettings.security.jwt
        val tokenLifetimeSec: Long = jwtSettings.tokenLifetimeSec
        val expirationDate = Date(System.currentTimeMillis() + tokenLifetimeSec.seconds.inWholeMilliseconds)
        val sessionContextJson: String = Json.encodeToString(
            serializer = SessionContext.serializer(),
            value = sessionContext
        )

        tracer.debug("Generating new authorization token. Expiration: $expirationDate.")

        return JWT.create()
            .withClaim(SessionContext.CLAIM_KEY, sessionContextJson)
            .withAudience(jwtSettings.audience)
            .withIssuer(jwtSettings.issuer)
            .withExpiresAt(expirationDate)
            .sign(Algorithm.HMAC256(jwtSettings.secretKey))
    }
}
