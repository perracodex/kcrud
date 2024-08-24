/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.access.actor.entity

import kcrud.base.persistence.serializers.UuidS
import kotlinx.serialization.Serializable

/**
 * Entity to create/update an Actor. An Actor is a user with a specific role and designated access to scopes.
 *
 * Regarding the [isLocked] flag:
 * This flag represents whether the Actor is currently locked out of the system, suspending all their
 * privileges and access regardless of their assigned role. While this is a critical security feature,
 * the flag is exposed in the API for scenarios where initial states need to be programmatically set, such
 * as during batch imports of new actors who might be pre-configured as locked due to pending verifications
 * or other conditions. This exposure allows for flexibility in managing actors' states through automated
 * processes while maintaining the guideline that unlocking should primarily be controlled by the server's
 * internal logic to prevent security breaches.
 *
 * Caution: Manipulating the [isLocked] flag through public-facing APIs can lead to potential security risks.
 * It is advisable to restrict the ability to unlock an actor to internal mechanisms only, ensuring that any
 * changes to this status through external endpoints are rigorously validated and securely handled.
 *
 * @property roleId The target Actor's role id.
 * @property username The Actor's unique username.
 * @property password The Actor's password in plaintext. Must ensure secure handling and storage.
 * @property isLocked Whether the Actor is locked, so its role and scope rules are ignored, loosing all accesses.
 */
@Serializable
data class ActorRequest(
    val roleId: UuidS,
    val username: String,
    val password: String,
    val isLocked: Boolean
)
