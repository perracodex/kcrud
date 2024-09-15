/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.env

import io.ktor.server.application.*
import io.ktor.server.auth.*
import kcrud.base.persistence.utils.toUuid
import kcrud.base.plugins.Uuid
import kcrud.base.settings.AppSettings
import kotlinx.serialization.Serializable

/**
 * Data class holding concrete session context information passed around in the application
 * when an Actor is authenticated.
 *
 * Passwords are never stored in the session context. Instead, these are securely hashed and cached
 * in-memory for fast and secure authentication, so that the application never has access to any
 * plain-text password, nor has to keep querying the database for credentials.
 *
 * The [schema] can be used to specify a database schema to use for the session. This is useful
 * for multi-tenant applications where each tenant has its own schema.
 *
 * @property actorId The unique actor identifier.
 * @property username The unique username, which can be null or blank for anonymous actor.
 * @property roleId The associated role id.
 * @property schema Optional database schema to use for the session.
 */
@Serializable
public data class SessionContext(
    val actorId: Uuid,
    val username: String,
    val roleId: Uuid,
    val schema: String? = null
) : Principal {
    public companion object {
        /**
         * Specifies the cookie name used for session tracking. This name is used to reference
         * the cookie that carries session context information across requests.
         */
        public const val SESSION_NAME: String = "_ctx"

        /**
         * Identifies the key under which session context data is stored within JWT claims.
         * This key is used to extract session context details from the token payload.
         */
        public const val CLAIM_KEY: String = "session_context"

        /**
         * The default empty session context instance. when security is disabled
         * or the actor is not authenticated.
         */
        private val defaultEmptyInstance: SessionContext by lazy {
            SessionContext(
                actorId = "00000000-0000-0000-0000-000000000000".toUuid(),
                username = "no-actor",
                roleId = "00000000-0000-0000-0000-000000000000".toUuid(),
                schema = null
            )
        }

        /**
         * Retrieves an existing [SessionContext] from the current authentication principal
         * or provides a default one based on security settings.
         *
         * First attempts to retrieve a [SessionContext] from the [ApplicationCall]'s principal.
         * If [SessionContext] is not present:
         *  - If security is disabled, it returns a default [SessionContext] with predefined 'empty' values.
         *  - If security is enabled, it returns null, indicating an unauthorized request.
         *
         * @param call The [ApplicationCall] associated with the current request, containing potential authentication data.
         * @return A [SessionContext] representing either the authenticated actor,
         *         or a default context when unauthenticated and security is disabled.
         */
        public fun from(call: ApplicationCall): SessionContext? {
            return call.principal<SessionContext>()
                ?: if (AppSettings.security.isEnabled) null else defaultEmptyInstance
        }
    }
}