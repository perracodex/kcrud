/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.env

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.sessions.*
import io.ktor.util.*
import kcrud.base.persistence.utils.toUuid
import kcrud.base.plugins.Uuid
import kcrud.base.settings.AppSettings
import kotlinx.serialization.Serializable

/**
 * Data class holding concrete [SessionContext] information passed around in the application
 * when an Actor is authenticated.
 *
 * Passwords are never stored in the [SessionContext]. Instead, these are securely hashed and cached
 * in-memory for fast and secure authentication, so that the application never has access to any
 * plain-text password, nor has to keep querying the database for credentials.
 *
 * The [schema] can be used to specify a database schema to use for the session. This is useful
 * for multi-tenant applications where each tenant has its own schema.
 *
 * @property actorId The unique actor identifier.
 * @property username The unique username, which can be null or blank for anonymous actor.
 * @property roleId The associated role id for the actor.
 * @property schema Optional database schema targeted by the session. For example in multi-tenant applications.
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
         * the cookie that carries [SessionContext] information across requests.
         */
        public const val SESSION_NAME: String = "_ctx"

        /**
         * Identifies the key under which [SessionContext] data is stored within JWT claims.
         * This key is used to extract [SessionContext] details from the token payload.
         */
        public const val CLAIM_KEY: String = "session_context"

        /**
         * Attribute key for storing the [SessionContext] into the [ApplicationCall] attributes.
         */
        private val SESSION_CONTEXT_KEY: AttributeKey<SessionContext> = AttributeKey(name = "SessionContext")

        /**
         * The default empty [SessionContext] instance. when security is disabled
         * or the actor is not authenticated.
         */
        private val emptySessionContext: SessionContext by lazy {
            val uuid: Uuid = "00000000-0000-0000-0000-000000000000".toUuid()
            SessionContext(
                actorId = uuid,
                username = "no-actor",
                roleId = uuid,
                schema = null
            )
        }

        /**
         * Extension function to add the provided [SessionContext] into the given [call] attributes,
         * in addition to setting it in the [Sessions] for tracking across requests.
         *
         * @param sessionContext The [SessionContext] to be added.
         */
        public fun ApplicationCall.setContext(sessionContext: SessionContext) {
            this.attributes.put(SESSION_CONTEXT_KEY, sessionContext)
            this.sessions.set(name = SESSION_NAME, value = sessionContext)
        }

        /**
         * Extension function to retrieve the [SessionContext] from the [ApplicationCall] attributes.
         *
         * This function simplifies accessing a [SessionContext] which is akin to the type-safe retrieval as follows:
         * ```
         * call.principal<SessionContext>()
         * ```
         * Authentication plugins set a [SessionContext] as the principal when authorizations are successful.
         * However, directly fetching it from the call's principal without considering the below retrieval
         * flow is not recommended.
         *
         * Retrieval Flow:
         * - Attempt to retrieve a [SessionContext] from the current call attributes.
         *      - If [SessionContext] is present in the call, return it as is.
         *      - If [SessionContext] is not present:
         *          - If security is enabled, return null, indicating an unauthorized request.
         *          - If security is disabled, return a default [SessionContext] with predefined 'empty' values.
         *
         * @return A [SessionContext] representing the authenticated actor;
         * `null` if unauthorized; or a default empty [SessionContext] when security is disabled.
         */
        public fun ApplicationCall.getContext(): SessionContext? =
            this.attributes.getOrNull(SESSION_CONTEXT_KEY)
                ?: if (AppSettings.security.isEnabled) null else emptySessionContext
    }
}
