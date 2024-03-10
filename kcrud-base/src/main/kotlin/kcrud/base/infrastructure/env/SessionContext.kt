/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.infrastructure.env

import com.auth0.jwt.interfaces.Payload
import io.ktor.server.auth.*
import kcrud.base.admin.actor.entities.ActorEntity
import kcrud.base.admin.actor.service.ActorService
import kcrud.base.persistence.serializers.SUUID
import kcrud.base.security.service.CredentialService
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.koin.mp.KoinPlatform.getKoin

/**
 * Data class holding concrete session context information passed around in the application
 * when an Actor is authenticated.
 *
 * Passwords are never stored in the session context. Instead, these are securely cached in-memory
 * by the [CredentialService], for fast and secure authentication. Passwords are hashed in-memory so
 * that the application never has access to any plain-text password.
 *
 * The schema can be used to specify a database schema to use for the session. This is useful for
 * multi-tenant applications where each tenant has its own schema.
 *
 * @property actorId The unique actor identifier.
 * @property username The unique username, which can be null or blank for anonymous actor.
 * @property roleId The associated role id.
 * @property schema Optional database schema to use for the session.
 */
@Serializable
data class SessionContext(
    val actorId: SUUID,
    val username: String,
    val roleId: SUUID,
    val schema: String? = null
) : Principal {

    companion object {
        /** The name of the sessions, so for cookies it will be the cookie name. */
        const val SESSION_NAME: String = "_ctx"

        /** The key used to store the session context in the payload claim. */
        const val CLAIM_KEY: String = "session_context"

        /**
         * Creates a [SessionContext] instance from a JWT [Payload].
         *
         * @param jwtPayload The JWT [Payload] containing actor-related claims.
         * @return A [SessionContext] instance if both actorId and role are present and valid, null otherwise.
         */
        fun from(jwtPayload: Payload): SessionContext? {
            val payload: String? = jwtPayload.getClaim(CLAIM_KEY)?.asString()

            return payload.takeIf { !it.isNullOrBlank() }?.let {
                Json.decodeFromString(deserializer = serializer(), string = it).run {
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
            val actorService: ActorService = getKoin().get()
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
}
