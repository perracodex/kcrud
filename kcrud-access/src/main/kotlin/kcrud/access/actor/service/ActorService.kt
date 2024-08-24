/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.access.actor.service

import kcrud.access.actor.entity.ActorEntity
import kcrud.access.actor.entity.ActorRequest
import kcrud.access.actor.repository.IActorRepository
import kcrud.access.credential.CredentialService
import kcrud.access.rbac.repository.role.IRbacRoleRepository
import kcrud.access.rbac.service.RbacService
import kcrud.base.env.Tracer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.uuid.Uuid

/**
 * Service to handle Actor operations.
 *
 * @property actorRepository The [IActorRepository] to handle Actor persistence operations.
 * @property roleRepository The [IRbacRoleRepository] to handle RBAC role operations.
 */
class ActorService(
    private val actorRepository: IActorRepository,
    private val roleRepository: IRbacRoleRepository
) : KoinComponent {
    private val tracer = Tracer<ActorService>()

    /**
     * Finds the [ActorEntity] for the given username.
     * @param username The username of the [ActorEntity] to find.
     * @return The [ActorEntity] for the given username, or null if it doesn't exist.
     */
    suspend fun findByUsername(username: String): ActorEntity? = withContext(Dispatchers.IO) {
        return@withContext actorRepository.findByUsername(username = username)
    }

    /**
     * Finds the [ActorEntity] for the given id.
     *
     * @param actorId The id of the [ActorEntity] to find.
     * @return The [ActorEntity] for the given id, or null if it doesn't exist.
     */
    suspend fun findById(actorId: Uuid): ActorEntity? = withContext(Dispatchers.IO) {
        return@withContext actorRepository.findById(actorId = actorId)
    }

    /**
     * Finds all existing [ActorEntity] entries.
     * @return A list with all existing [ActorEntity] entries.
     */
    suspend fun findAll(): List<ActorEntity> = withContext(Dispatchers.IO) {
        return@withContext actorRepository.findAll()
    }

    /**
     * Creates a new [ActorEntity].
     * @param actorRequest The [ActorRequest] to create.
     * @return The id of the [ActorEntity] created.
     */
    suspend fun create(actorRequest: ActorRequest): Uuid = withContext(Dispatchers.IO) {
        tracer.debug("Creating actor with username: ${actorRequest.username}")
        val actorId: Uuid = actorRepository.create(actorRequest = actorRequest)
        refresh(actorId = actorId)
        return@withContext actorId
    }

    /**
     * Updates an existing [ActorEntity].
     *
     * @param actorId The id of the Actor to update.
     * @param actorRequest The new details for the [ActorEntity].
     * @return How many records were updated.
     */
    suspend fun update(actorId: Uuid, actorRequest: ActorRequest): Int = withContext(Dispatchers.IO) {
        tracer.debug("Updating actor with ID: $actorId")
        val count: Int = actorRepository.update(actorId = actorId, actorRequest = actorRequest)
        refresh(actorId = actorId)
        return@withContext count
    }

    /**
     * Sets the lock status for the given [ActorEntity].
     *
     * @param actorId The id of the [ActorEntity] to lock/unlock.
     * @param isLocked Whether the [ActorEntity] should be locked or unlocked.
     */
    @Suppress("unused")
    suspend fun setLockedState(actorId: Uuid, isLocked: Boolean): Unit = withContext(Dispatchers.IO) {
        tracer.debug("Setting lock state for actor with ID: $actorId")
        actorRepository.setLockedState(actorId = actorId, isLocked = isLocked)
        refresh(actorId = actorId)
    }

    /**
     * Checks if there are any Actors in the database, or if the given usernames exist.
     *
     * @param usernames The actors usernames to check for. If null, checks for any Actors.
     * @return True if there are actors for the given usernames in the database, false otherwise.
     */
    suspend fun actorsExist(usernames: List<String>? = null): Boolean = withContext(Dispatchers.IO) {
        return@withContext actorRepository.actorsExist(usernames = usernames)
    }

    /**
     * Refreshes the [CredentialService] and [RbacService] to reflect an Actor creation or update.
     *
     * @param actorId The id of the Actor to refresh.
     */
    private suspend fun refresh(actorId: Uuid) {
        tracer.info("Triggering refresh in Credentials and RBAC services for actor with ID: $actorId")

        val credentialService: CredentialService by inject()
        credentialService.refresh(actorId = actorId)

        val rbacService: RbacService by inject()
        rbacService.refresh(actorId = actorId)
    }
}
