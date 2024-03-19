/*
 * Copyright (c) 2024-Present Perraco. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.env

import io.ktor.server.auth.*
import kcrud.base.persistence.serializers.SUUID
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
    }
}