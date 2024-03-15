/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.access.system

import com.auth0.jwt.interfaces.Payload
import kcrud.access.actor.entity.ActorEntity
import kcrud.access.actor.service.ActorService
import kcrud.base.env.SessionContext
import kotlinx.serialization.json.Json
import org.koin.mp.KoinPlatform

/**
 * Factory class for creating [SessionContext] instances.
 */
object SessionContextFactory {

    /**
     * Creates a [SessionContextFactory] instance from a JWT [Payload].
     *
     * @param jwtPayload The JWT [Payload] containing actor-related claims.
     * @return A [SessionContextFactory] instance if both actorId and role are present and valid, null otherwise.
     */
    fun from(jwtPayload: Payload): SessionContext? {
        val payload: String? = jwtPayload.getClaim(SessionContext.CLAIM_KEY)?.asString()

        return payload.takeIf { !it.isNullOrBlank() }?.let {
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
     * Retrieves a [SessionContextFactory] instance from the database given a username.
     *
     * @param username The username of the Actor to retrieve.
     * @return A [SessionContextFactory] instance if the Actor exists, null otherwise.
     */
    suspend fun from(username: String): SessionContext? {
        val actorService: ActorService = KoinPlatform.getKoin().get()
        val actor: ActorEntity? = actorService.findByUsername(username = username)

        return actor?.let {
            SessionContext(
                actorId = it.id,
                username = it.username,
                roleId = it.role.id
            )
        }
    }
}