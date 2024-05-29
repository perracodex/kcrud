/*
 * Copyright (c) 2024-Present Perracodex. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.security.hash

/**
 * Represents a securely hashed value and its associated salt.
 * This class ensures that hashing operations meet security
 * standards by using a unique salt for each hash.
 *
 * @property hash The binary representation of the hashed value.
 * @property salt The associated SecureSalt instance used during the hashing process.
 */
data class SecureHash(val hash: ByteArray, val salt: SecureSalt) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as SecureHash
        return hash.contentEquals(other = other.hash) && (salt == other.salt)
    }

    override fun hashCode(): Int = arrayOf(salt, hash).contentHashCode()
}
