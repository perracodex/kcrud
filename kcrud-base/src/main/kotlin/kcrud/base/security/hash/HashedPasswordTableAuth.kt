/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.security.hash

import io.ktor.server.auth.*
import io.ktor.util.*

/**
 * Provides authentication functionality by storing and validating hashed passwords with salts.
 *
 * It is designed based on the principle of [UserHashedTableAuth], but extends its functionality
 * by incorporating a unique salt per password into the hashing process.
 * This approach prevents identical passwords from resulting in the same hash and safeguards against
 * certain types of brute force attacks.
 *
 * @property digester A function that, given a password and a salt, produces a [SecureHash] containing
 * the hashed password and the used salt.
 * @property table A map associating usernames (as keys) with their corresponding [SecureHash] instances.
 * This table serves as the storage for the hashed passwords and their salts, enabling the authentication
 * process.
 */
internal class HashedPasswordTableAuth(
    private val digester: (password: String, salt: SecureSalt) -> SecureHash,
    private val table: Map<String, SecureHash>
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
        /**
         * The algorithm used to hash passwords.
         */
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
