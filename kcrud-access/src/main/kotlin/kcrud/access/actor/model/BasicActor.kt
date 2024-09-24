/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.access.actor.model

import kotlin.uuid.Uuid

/**
 * Represents teh basic details of single Actor. An Actor is a user with a role and access to scopes.
 *
 * @property id The Actor's unique id.
 * @property username The Actor's unique username.
 * @property roleId The associated RBAC role id.
 * @property isLocked Whether the Actor is locked, so its role and associated rules are ignored, loosing all accesses.
 *
 * @see [Actor]
 */
internal data class BasicActor(
    var id: Uuid,
    val username: String,
    val roleId: Uuid,
    val isLocked: Boolean,
)
