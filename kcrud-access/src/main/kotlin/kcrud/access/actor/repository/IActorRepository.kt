/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.access.actor.repository

import kcrud.access.actor.model.Actor
import kcrud.access.actor.model.ActorRequest
import kotlin.uuid.Uuid

/**
 * Repository responsible for [Actor] data.
 */
internal interface IActorRepository {

    /**
     * Finds the [Actor] for the given username.
     *
     * @param username The username of the [Actor] to find.
     * @return The [Actor] for the given username, or null if it doesn't exist.
     */
    suspend fun findByUsername(username: String): Actor?

    /**
     * Finds all existing [Actor] entries.
     * @return A list with all existing [Actor] entries.
     */
    suspend fun findAll(): List<Actor>

    /**
     * Finds the [Actor] for the given id.
     *
     * @param actorId The id of the [Actor] to find.
     * @return The [Actor] for the given id, or null if it doesn't exist.
     */
    suspend fun findById(actorId: Uuid): Actor?

    /**
     * Creates a new [Actor].
     * @param actorRequest The [ActorRequest] to create.
     * @return The id of the [Actor] created.
     */
    suspend fun create(actorRequest: ActorRequest): Uuid

    /**
     * Updates an existing [Actor].
     *
     * @param actorId The id of the actor to update.
     * @param actorRequest The new details for the [Actor].
     * @return How many records were updated.
     */
    suspend fun update(actorId: Uuid, actorRequest: ActorRequest): Int

    /**
     * Sets the lock status for the given [Actor].
     *
     * @param actorId The id of the [Actor] to lock/unlock.
     * @param isLocked Whether the [Actor] should be locked or unlocked.
     */
    suspend fun setLockedState(actorId: Uuid, isLocked: Boolean)

    /**
     * Checks if there are any Actor in the database, or if the given usernames exist.
     *
     * @param usernames The actors usernames to check for. If null, checks for any actors.
     * @return True if there are actors for the given usernames in the database, false otherwise.
     */
    suspend fun actorsExist(usernames: List<String>? = null): Boolean
}
