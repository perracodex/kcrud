/*
 * Copyright (c) 2023-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.access.actor.entities

import kcrud.base.persistence.serializers.SUUID
import kotlinx.serialization.Serializable

/**
 * Entity to create/update an Actor. An Actor is a user with a role and access to resources.
 *
 * @property roleId The target Actor's role id.
 * @property username The unique Actor's username.
 * @property password The unencrypted Actor's password.
 * @property isLocked Whether the Actor is locked, so its role and resource rules are ignored, loosing all accesses.
 */
@Serializable
data class ActorRequest(
    val roleId: SUUID,
    val username: String,
    val password: String,
    val isLocked: Boolean
)
