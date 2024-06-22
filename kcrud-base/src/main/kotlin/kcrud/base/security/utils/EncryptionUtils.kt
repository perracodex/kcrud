/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.security.utils

import kcrud.base.settings.AppSettings
import kcrud.base.settings.config.sections.security.sections.EncryptionSettings
import org.jetbrains.exposed.crypt.Algorithms
import org.jetbrains.exposed.crypt.Encryptor

/**
 * Utility class for database field encryption.
 */
object EncryptionUtils {
    private enum class AlgorithmName {
        AES_256_PBE_CBC,
        AES_256_PBE_GCM,
        BLOW_FISH,
        TRIPLE_DES
    }

    enum class Type {
        /** Stable encryption for data at rest, such as encrypted database fields. */
        AT_REST,

        /** Transient encryption for data in transit, such as encrypted URLs. */
        AT_TRANSIT
    }

    /**
     * Get the [Encryptor] based on the encryption configuration settings.
     * Used for example to encrypt database fields.
     *
     * @param type The target [EncryptionUtils.Type] of encryption to use.
     */
    fun getEncryptor(type: Type): Encryptor {
        val encryptionSpec: EncryptionSettings.Spec = when (type) {
            Type.AT_REST -> AppSettings.security.encryption.atRest
            Type.AT_TRANSIT -> AppSettings.security.encryption.atTransit
        }

        val algorithm: AlgorithmName = AlgorithmName.valueOf(encryptionSpec.algorithm)
        val key: String = encryptionSpec.key
        val salt: String = encryptionSpec.salt

        return when (algorithm) {
            AlgorithmName.AES_256_PBE_CBC -> Algorithms.AES_256_PBE_CBC(password = key, salt = salt)
            AlgorithmName.AES_256_PBE_GCM -> Algorithms.AES_256_PBE_GCM(password = key, salt = salt)
            AlgorithmName.BLOW_FISH -> Algorithms.BLOW_FISH(key = key)
            AlgorithmName.TRIPLE_DES -> Algorithms.TRIPLE_DES(secretKey = key)
        }
    }
}
