/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.access.actor.repository

import kcrud.access.actor.model.ActorDto
import kcrud.access.actor.model.ActorRequest
import kotlin.uuid.Uuid

/**
 * Repository responsible for [ActorDto] data.
 */
internal interface IActorRepository {

    /**
     * Finds the [ActorDto] for the given username.
     *
     * @param username The username of the [ActorDto] to find.
     * @return The [ActorDto] for the given username, or null if it doesn't exist.
     */
    suspend fun findByUsername(username: String): ActorDto?

    /**
     * Finds all existing [ActorDto] entries.
     * @return A list with all existing [ActorDto] entries.
     */
    suspend fun findAll(): List<ActorDto>

    /**
     * Finds the [ActorDto] for the given id.
     *
     * @param actorId The id of the [ActorDto] to find.
     * @return The [ActorDto] for the given id, or null if it doesn't exist.
     */
    suspend fun findById(actorId: Uuid): ActorDto?

    /**
     * Creates a new [ActorDto].
     * @param actorRequest The [ActorRequest] to create.
     * @return The id of the [ActorDto] created.
     */
    suspend fun create(actorRequest: ActorRequest): Uuid

    /**
     * Updates an existing [ActorDto].
     *
     * @param actorId The id of the actor to update.
     * @param actorRequest The new details for the [ActorDto].
     * @return How many records were updated.
     */
    suspend fun update(actorId: Uuid, actorRequest: ActorRequest): Int

    /**
     * Sets the lock status for the given [ActorDto].
     *
     * @param actorId The id of the [ActorDto] to lock/unlock.
     * @param isLocked Whether the [ActorDto] should be locked or unlocked.
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
