/*
 * Copyright (c) 2023-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.access.actor.repository

import kcrud.access.actor.entity.ActorEntity
import kcrud.access.actor.entity.ActorRequest
import java.util.*

/**
 * Repository responsible for [ActorEntity] data.
 */
interface IActorRepository {

    /**
     * Finds the [ActorEntity] for the given username.
     *
     * @param username The username of the [ActorEntity] to find.
     * @return The [ActorEntity] for the given username, or null if it doesn't exist.
     */
    suspend fun findByUsername(username: String): ActorEntity?

    /**
     * Finds all existing [ActorEntity] entries.
     * @return A list with all existing [ActorEntity] entries.
     */
    suspend fun findAll(): List<ActorEntity>

    /**
     * Finds the [ActorEntity] for the given id.
     *
     * @param actorId The id of the [ActorEntity] to find.
     * @return The [ActorEntity] for the given id, or null if it doesn't exist.
     */
    suspend fun findById(actorId: UUID): ActorEntity?

    /**
     * Creates a new [ActorEntity].
     * @param actorRequest The [ActorRequest] to create.
     * @return The id of the [ActorEntity] created.
     */
    suspend fun create(actorRequest: ActorRequest): UUID

    /**
     * Updates an existing [ActorEntity].
     *
     * @param actorId The id of the actor to update.
     * @param actorRequest The new details for the [ActorEntity].
     * @return How many records were updated.
     */
    suspend fun update(actorId: UUID, actorRequest: ActorRequest): Int

    /**
     * Sets the lock status for the given [ActorEntity].
     *
     * @param actorId The id of the [ActorEntity] to lock/unlock.
     * @param isLocked Whether the [ActorEntity] should be locked or unlocked.
     */
    suspend fun setLockedState(actorId: UUID, isLocked: Boolean)

    /**
     * Checks if there are any Actor in the database, or if the given usernames exist.
     *
     * @param usernames The actors usernames to check for. If null, checks for any actors.
     * @return True if there are actors for the given usernames in the database, false otherwise.
     */
    suspend fun actorsExist(usernames: List<String>? = null): Boolean
}
