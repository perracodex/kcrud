/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package krud.access.credential

import io.ktor.server.auth.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import krud.access.domain.actor.model.ActorCredentials
import krud.access.domain.actor.service.ActorService
import krud.base.env.Tracer
import krud.base.security.hash.SecureHash
import krud.base.security.hash.SecureSalt
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.ConcurrentHashMap
import kotlin.uuid.Uuid

/**
 * Class managing Actor credential authentication, providing in-memory storage for username
 * and hashed password pairs.
 * It leverages [HashedPasswordTableAuth], a custom class based in Ktor's [UserHashedTableAuth].
 *
 * This approach enhances security by avoiding the need to work with plain-text passwords
 * and speeds up authentication by eliminating database lookups on each request.
 *
 * Password hashing is done using SHA-256 and a random salt per password, so two identical
 * passwords will produce different hashes due to their unique salts.
 *
 * #### References
 * - [Ktor UserHashedTableAuth](https://ktor.io/docs/server-basic-auth.html#validate-user-hash)
 *
 * @see [HashedPasswordTableAuth]
 */
internal class CredentialService : KoinComponent {
    private val tracer: Tracer = Tracer<CredentialService>()

    /** Lock to ensure thread-safe access and updates to the actor mapping cache. */
    private val lock: Mutex = Mutex()

    /** Mapping of usernames to hashed passwords. */
    private val cache: ConcurrentHashMap<String, SecureHash> = ConcurrentHashMap()

    /** Provides the in-memory authentication datastore. */
    private val store: HashedPasswordTableAuth = HashedPasswordTableAuth(
        table = cache,
        digester = { password, salt ->
            HashedPasswordTableAuth.hashPassword(
                password = password,
                salt = salt
            )
        }
    )

    /**
     * Authenticates an Actor based on the provided credentials.
     */
    suspend fun authenticate(credential: UserPasswordCredential): UserIdPrincipal? {
        if (cache.isEmpty()) {
            refreshActors()
        }

        return if (cache.isEmpty()) {
            null
        } else {
            store.authenticate(credential = credential)
        }
    }

    /**
     * Refreshes the cached credentials for a single actor.
     * This operation implies generating a new hash for the actor's password in the memory cache.
     *
     * @param actorId The id of the Actor to refresh.
     * @throws IllegalStateException If no actor is found with the provided id.
     */
    suspend fun refreshActor(actorId: Uuid) {
        tracer.info("Refreshing credentials cache for actor with ID: $actorId")

        val actorService: ActorService by inject()
        val actorCredentials: ActorCredentials? = actorService.findCredentials(actorId = actorId)
        checkNotNull(actorCredentials) { "No actor found with id $actorId." }

        val hashedPassword: SecureHash = HashedPasswordTableAuth.hashPassword(
            password = actorCredentials.password,
            salt = SecureSalt.generate()
        )

        lock.withLock {
            cache[actorCredentials.username.lowercase()] = hashedPassword
        }

        tracer.info("Refreshed credentials cache for actor with ID: $actorId")
    }

    /**
     * Refreshes the cached credentials by clearing it and reloading from the database.
     * This operation implies generating a new hash for all actors passwords in the memory cache.
     *
     * @throws IllegalStateException If no actors are found in the system.
     */
    suspend fun refreshActors() {
        tracer.info("Refreshing credentials cache for all actors.")

        val actorService: ActorService by inject()
        val actors: List<ActorCredentials> = actorService.findAllCredentials()
        check(actors.isNotEmpty()) { "No actors found in the system." }

        val newCache: ConcurrentHashMap<String, SecureHash> = actors.associateTo(ConcurrentHashMap()) { actor ->
            actor.username.lowercase() to HashedPasswordTableAuth.hashPassword(
                password = actor.password,
                salt = SecureSalt.generate()
            )
        }

        // Replace the current cache with the new one if refreshing all actors.
        lock.withLock {
            cache.clear()
            cache.putAll(newCache)
        }

        tracer.info("Refreshed credentials cache for all actors.")
    }

    companion object {
        /**
         * Naive hint for Actors login, only for this example project.
         * This is not something would do in a real application.
         */
        const val HINT: String = "Use either admin/admin or guest/guest."
    }
}
