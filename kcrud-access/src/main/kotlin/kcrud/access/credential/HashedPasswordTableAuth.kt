/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.access.credential

import io.ktor.server.auth.*
import io.ktor.util.*
import kcrud.core.security.hash.SecureHash
import kcrud.core.security.hash.SecureSalt

/**
 * Provides authentication functionality by storing and validating hashed passwords with salts.
 *
 * It is designed based on the principle of [UserHashedTableAuth], but extends its functionality
 * by incorporating a unique salt per password into the hashing process.
 * This approach prevents identical passwords from resulting in the same hash and safeguards against
 * certain types of brute force attacks.
 *
 * @property table A map associating usernames (as keys) with their corresponding [SecureHash] instances.
 *                 This table serves as the storage for the hashed passwords and their salts,
 *                 enabling the authentication process.
 * @property digester A function that, given a password and a salt, produces a [SecureHash] containing
 *                    the hashed password and the used salt.
 */
internal class HashedPasswordTableAuth(
    private val table: Map<String, SecureHash>,
    private val digester: (password: String, salt: SecureSalt) -> SecureHash
) {
    fun authenticate(credential: UserPasswordCredential): UserIdPrincipal? {
        val storedHash: SecureHash = table[credential.name.lowercase()] ?: return null
        val attemptedHash: SecureHash = digester(credential.password, storedHash.salt)

        if (attemptedHash == storedHash) {
            return UserIdPrincipal(name = credential.name)
        }

        return null
    }

    companion object {
        /** The algorithm used to hash passwords. */
        private const val ALGORITHM: String = "SHA-256"

        /**
         * Factory method to hash a given password into new [SecureHash].
         *
         * @param password The plaintext password to be hashed.
         * @param salt The unique [SecureHash] to be combined with the password before hashing.
         * @return A new instance of [SecureHash] with the hashed password and the used salt.
         */
        fun hashPassword(password: String, salt: SecureSalt): SecureHash {
            val hash: ByteArray = getDigestFunction(algorithm = ALGORITHM) {
                salt.saltValueToString()
            }(password)

            return SecureHash(hash = hash, salt = salt)
        }
    }
}
