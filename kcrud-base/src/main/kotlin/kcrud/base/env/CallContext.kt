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
 * Data class holding concrete [CallContext] information passed around in the application
 * when an Actor is authenticated.
 *
 * Passwords are never stored in the [CallContext]. Instead, these are securely hashed and cached
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
public data class CallContext(
    val actorId: Uuid,
    val username: String,
    val roleId: Uuid,
    val schema: String? = null
) : Principal {
    public companion object {
        /**
         * Specifies the cookie name used for session tracking. This name is used to reference
         * the cookie that carries [CallContext] information across requests.
         */
        public const val SESSION_NAME: String = "_ctx"

        /**
         * Identifies the key under which [CallContext] data is stored within JWT claims.
         * This key is used to extract [CallContext] details from the token payload.
         */
        public const val CLAIM_KEY: String = "call_context"

        /**
         * Attribute key for storing the [CallContext] into the [ApplicationCall] attributes.
         */
        private val CALL_CONTEXT_KEY: AttributeKey<CallContext> = AttributeKey(name = "CallContext")

        /**
         * The default empty [CallContext] instance. when security is disabled
         * or the actor is not authenticated.
         */
        private val emptyCallContext: CallContext by lazy {
            val uuid: Uuid = "00000000-0000-0000-0000-000000000000".toUuid()
            CallContext(
                actorId = uuid,
                username = "no-actor",
                roleId = uuid,
                schema = null
            )
        }

        /**
         * Extension function to add the provided [CallContext] into the given [call] attributes,
         * in addition to setting it in the [Sessions] for tracking across requests.
         *
         * @param callContext The [CallContext] to be added.
         */
        public fun ApplicationCall.setContext(callContext: CallContext) {
            this.attributes.put(CALL_CONTEXT_KEY, callContext)
            this.sessions.set(name = SESSION_NAME, value = callContext)
        }

        /**
         * Extension function to retrieve the [CallContext] from the [ApplicationCall] attributes.
         *
         * This function simplifies accessing a [CallContext] which is akin to the type-safe retrieval as follows:
         * ```
         * call.principal<CallContext>()
         * ```
         * Authentication plugins set a [CallContext] as the principal when authorizations are successful.
         * However, directly fetching it from the call's principal without considering the below retrieval
         * flow is not recommended.
         *
         * Retrieval Flow:
         * - Attempt to retrieve a [CallContext] from the current call attributes.
         *      - If [CallContext] is present in the call, return it as is.
         *      - If [CallContext] is not present:
         *          - If security is enabled, return null, indicating an unauthorized request.
         *          - If security is disabled, return a default [CallContext] with predefined 'empty' values.
         *
         * @return A [CallContext] representing the authenticated actor;
         * `null` if unauthorized; or a default empty [CallContext] when security is disabled.
         */
        public fun ApplicationCall.getContext(): CallContext? =
            this.attributes.getOrNull(CALL_CONTEXT_KEY)
                ?: if (AppSettings.security.isEnabled) null else emptyCallContext
    }
}
