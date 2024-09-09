/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
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
import kcrud.base.env.SessionContext
import kcrud.base.env.Tracer
import kcrud.base.settings.AppSettings
import kcrud.base.settings.config.sections.security.sections.auth.JwtAuthSettings
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*
import kotlin.time.Duration.Companion.seconds

/**
 * Security class responsible for verifying JWT tokens.
 *
 * This class contains the logic for verifying JWT tokens using HMAC256 algorithm.
 * If the token is invalid or any exception occurs, an UnauthorizedException will be thrown.
 */
internal object AuthenticationTokenService {
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
     *
     * @param headers The request [Headers] to extract the token from.
     */
    fun getState(headers: Headers): TokenState {
        return try {
            val token: String = fromHeader(headers = headers)
            val algorithm: Algorithm = Algorithm.HMAC256(AppSettings.security.jwtAuth.secretKey)
            val verifier: JWTVerifier = JWT.require(algorithm).build()
            val decodedToken = JWT.decode(token)
            verifier.verify(decodedToken)
            TokenState.VALID
        } catch (e: TokenExpiredException) {
            tracer.info("Token expired: ${e.message}")
            TokenState.EXPIRED
        } catch (e: JWTDecodeException) {
            tracer.error("Failed to decode token: ${e.message}")
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
     *
     * @param headers The request [Headers] to extract the token from.
     */
    fun fromHeader(headers: Headers): String {
        val authHeader: String? = headers.entries().find { header ->
            header.key.equals(other = HttpHeaders.Authorization, ignoreCase = true)
        }?.value?.get(index = 0)

        require(!authHeader.isNullOrBlank() && authHeader.startsWith(prefix = AuthScheme.Bearer, ignoreCase = true)) {
            "Invalid Authorization header format. Expected format is 'Bearer <token>'."
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
        val jwtAuthSettings: JwtAuthSettings = AppSettings.security.jwtAuth
        val tokenLifetimeSec: Long = jwtAuthSettings.tokenLifetimeSec
        val expirationDate = Date(System.currentTimeMillis() + tokenLifetimeSec.seconds.inWholeMilliseconds)
        val sessionContextJson: String = Json.encodeToString<SessionContext>(value = sessionContext)

        tracer.debug("Generating new authorization token. Expiration: $expirationDate.")

        return JWT.create()
            .withClaim(SessionContext.CLAIM_KEY, sessionContextJson)
            .withAudience(jwtAuthSettings.audience)
            .withIssuer(jwtAuthSettings.issuer)
            .withExpiresAt(expirationDate)
            .sign(Algorithm.HMAC256(jwtAuthSettings.secretKey))
    }
}
