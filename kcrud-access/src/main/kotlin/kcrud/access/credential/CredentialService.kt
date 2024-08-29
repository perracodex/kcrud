/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
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
 * See: [Ktor UserHashedTableAuth](https://ktor.io/docs/server-basic-auth.html#validate-user-hash)
 *
 * @see HashedPasswordTableAuth
 */
public class CredentialService : KoinComponent {
    private val tracer = Tracer<CredentialService>()

    /** Lock to ensure thread-safe access and updates to the actor mapping cache. */
    private val lock = Mutex()

    /** Mapping of usernames to hashed passwords. */
    private val cache: ConcurrentHashMap<String, SecureHash> = ConcurrentHashMap()

    /** Provides the in-memory authentication store. */
    private val store: HashedPasswordTableAuth = HashedPasswordTableAuth(
        digester = { password, salt ->
            HashedPasswordTableAuth.hashPassword(
                password = password,
                salt = salt
            )
        },
        table = cache
    )

    /**
     * Authenticates an Actor based on the provided credentials.
     */
    public suspend fun authenticate(credential: UserPasswordCredential): UserIdPrincipal? {
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
    public suspend fun refresh(actorId: Uuid? = null): Unit = withContext(Dispatchers.IO) {
        tracer.info("Refreshing credentials cache.")

        val actorService: ActorService by inject()

        // Refresh a single actor or all actors based on the actorId parameter.
        val actorsToRefresh: List<ActorEntity> = actorId?.let {
            actorService.findById(actorId = actorId)?.let { listOf(it) } ?: emptyList()
        } ?: actorService.findAll()

        check(actorsToRefresh.isNotEmpty()) {
            "No actor(s) found for the given criteria."
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

            actorId?.let {
                // Update only the specified actor's cache entry.
                lock.withLock {
                    cache[actor.username.lowercase()] = hashedPassword
                }
            } ?: run {
                // Add the updated credentials to the new cache for later replacement of the entire cache.
                newCache?.put(key = actor.username.lowercase(), value = hashedPassword)
            }
        }

        // Replace the current cache with the new one if refreshing all actors.
        actorId ?: lock.withLock {
            cache.clear()
            cache.putAll(newCache!!)
        }

        tracer.info("Credentials cache refreshed.")
    }

    public companion object {
        /**
         * Naive hint for Actors login, only for this example project.
         * This is not something would do in a real application.
         */
        public const val HINT: String = "Use either admin/admin or guest/guest."
    }
}
