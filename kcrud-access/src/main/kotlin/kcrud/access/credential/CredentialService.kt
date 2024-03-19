/*
 * Copyright (c) 2024-Present Perraco. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.access.credential

import io.ktor.server.auth.*
import kcrud.access.actor.entity.ActorEntity
import kcrud.access.actor.service.ActorService
import kcrud.base.env.Tracer
import kcrud.base.security.hash.SecureHash
import kcrud.base.security.hash.SecureSalt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*
import java.util.concurrent.ConcurrentHashMap

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
 * See: [HashedPasswordTableAuth]
 *
 * See: [Ktor UserHashedTableAuth](https://ktor.io/docs/basic.html#validate-user-hash)
 */
class CredentialService : KoinComponent {
    private val tracer = Tracer<CredentialService>()

    /**
     * Lock to ensure thread-safe access and updates to the actor mapping cache.
     */
    private val lock = Mutex()

    /**
     * Mapping of usernames to hashed passwords.
     */
    private val cache: ConcurrentHashMap<String, SecureHash> = ConcurrentHashMap()

    /**
     * Provides the in-memory authentication store.
     */
    private val store: HashedPasswordTableAuth = HashedPasswordTableAuth(
        digester = { password, salt -> HashedPasswordTableAuth.hashPassword(password = password, salt = salt) },
        table = cache
    )

    /**
     * Authenticates an Actor based on the provided credentials.
     */
    suspend fun authenticate(credential: UserPasswordCredential): UserIdPrincipal? {
        return if (isCacheEmpty()) {
            null
        } else {
            store.authenticate(credential = credential)
        }
    }

    /**
     * Checks if the cache has been populated with credentials.
     * If empty, it refreshes it.
     *
     * @return True if the cache is empty, false if populated.
     */
    private suspend fun isCacheEmpty(): Boolean {
        if (cache.isEmpty()) {
            refresh()
        }

        return cache.isEmpty()
    }

    /**
     * Refreshes the cached credentials by clearing it and reloading from the database.
     * If an actorId is provided, only the credentials for that actor are refreshed;
     * otherwise all actors are refreshed.
     *
     * @param actorId The id of the Actor to refresh. If null, refreshes all actors.
     */
    suspend fun refresh(actorId: UUID? = null) = withContext(Dispatchers.IO) {
        tracer.info("Refreshing credentials cache.")

        val actorService: ActorService by inject()

        // Refresh a single actor or all actors based on the actorId parameter.
        val actorsToRefresh: List<ActorEntity> = if (actorId == null) {
            actorService.findAll()
        } else {
            actorService.findById(actorId = actorId)?.let { listOf(it) } ?: emptyList()
        }

        if (actorsToRefresh.isEmpty()) {
            throw IllegalStateException("No actor found for the given criteria.")
        }

        // Prepare the new cache mapping for the specified actors.
        // Null if refreshing a single actor since the existing cache will be updated directly.
        val newCache: ConcurrentHashMap<String, SecureHash>? = if (actorId == null) ConcurrentHashMap() else null

        actorsToRefresh.forEach { actor ->
            val salt: SecureSalt = SecureSalt.generate()
            val hashedPassword: SecureHash = HashedPasswordTableAuth.hashPassword(
                password = actor.password,
                salt = salt
            )

            if (actorId == null) {
                // Prepare to replace the entire cache for all actors.
                newCache?.put(key = actor.username.lowercase(), value = hashedPassword)
            } else {
                // Update only the specified actor's cache entry.
                lock.withLock {
                    cache[actor.username.lowercase()] = hashedPassword
                }
            }
        }

        // Replace the current cache with the new one if refreshing all actors.
        if (actorId == null) {
            lock.withLock {
                cache.clear()
                cache.putAll(newCache!!)
            }
        }

        tracer.info("Credentials cache refreshed.")
    }

    companion object {
        /**
         * Naive hint for Actors login, only for this example project.
         * This is not something you would do in a real application.
         */
        const val HINT: String = "Use either admin/admin or guest/guest."
    }
}
