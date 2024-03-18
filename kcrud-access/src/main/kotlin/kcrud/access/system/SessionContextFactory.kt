/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.access.system

import com.auth0.jwt.JWT
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.interfaces.Payload
import io.ktor.server.auth.*
import kcrud.access.actor.entity.ActorEntity
import kcrud.access.actor.service.ActorService
import kcrud.base.env.SessionContext
import kcrud.base.env.Tracer
import kcrud.base.settings.AppSettings
import kotlinx.serialization.json.Json
import org.koin.mp.KoinPlatform

/**
 * Factory class for creating [SessionContext] instances.
 */
object SessionContextFactory {
    private val tracer = Tracer<SessionContextFactory>()

    /**
     * Creates a [SessionContext] instance from a JWT [Payload].
     *
     * @param jwtPayload The JWT [Payload] containing actor-related claims.
     * @return A [SessionContext] instance if both actorId and role are present and valid, null otherwise.
     */
    fun from(jwtPayload: Payload): SessionContext? {
        val payload: String? = jwtPayload.getClaim(SessionContext.CLAIM_KEY)?.asString()

        if (payload.isNullOrBlank()) {
            tracer.error("Invalid JWT payload.")
            return null
        }

        return payload.let {
            Json.decodeFromString(deserializer = SessionContext.serializer(), string = it).run {
                SessionContext(
                    actorId = actorId,
                    username = username,
                    roleId = roleId,
                    schema = schema
                )
            }
        }
    }

    /**
     * Retrieves a [SessionContext] instance from the database given a username.
     *
     * @param username The username of the Actor to retrieve.
     * @return A [SessionContext] instance if the Actor exists, null otherwise.
     */
    suspend fun from(username: String): SessionContext? {
        val actorService: ActorService = KoinPlatform.getKoin().get()
        val actor: ActorEntity? = actorService.findByUsername(username = username)

        if (actor == null) {
            tracer.error("No actor found for username: $username")
            return null
        }

        return actor.let { actorDetails ->
            SessionContext(
                actorId = actorDetails.id,
                username = actorDetails.username,
                roleId = actorDetails.role.id
            )
        }
    }

    /**
     * Creates a [SessionContext] instance from an OAuth [OAuthAccessTokenResponse.OAuth2].
     *
     * @param oauth2 The OAuth [OAuthAccessTokenResponse.OAuth2] containing actor-related claims.
     * @return A [SessionContext] instance if both actorId and role are present and valid, null otherwise.
     */
    suspend fun from(oauth2: OAuthAccessTokenResponse.OAuth2): SessionContext? {
        val jwt: DecodedJWT? = try {
            JWT.decode(oauth2.extraParameters["id_token"] as String)
        } catch (e: Exception) {
            null
        }

        if (jwt == null || jwt.subject.isNullOrBlank()) {
            tracer.error("Invalid OAuth.")
            return null
        }

        if (!AppSettings.security.oauth.authorizeUrl.startsWith(jwt.issuer)) {
            tracer.error("Invalid OAuth issuer: ${jwt.issuer}")
            return null
        }

        if (jwt.audience.isEmpty() || jwt.audience[0] != AppSettings.security.oauth.clientId) {
            tracer.error("Invalid OAuth audience: ${jwt.audience}")
            return null
        }

        val username: String? = jwt.claims["name"]?.asString()
        if (username.isNullOrBlank()) {
            tracer.error("Invalid OAuth username: $username")
            return null
        }

        val actorService: ActorService = KoinPlatform.getKoin().get()
        val actor: ActorEntity? = actorService.findByUsername(username = username)
        if (actor == null) {
            tracer.error("No actor found for username: $username")
            return null
        }

        return actor.let { actorDetails ->
            SessionContext(
                actorId = actorDetails.id,
                username = actorDetails.username,
                roleId = actorDetails.role.id,
            )
        }
    }
}
