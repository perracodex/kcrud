/*
 * Copyright (c) 2024-Present Perraco. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
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

    /**
     * Get the [Encryptor] based on the encryption configuration settings.
     * Used for example to encrypt database fields.
     */
    fun getEncryptor(): Encryptor {
        val encryption: EncryptionSettings = AppSettings.security.encryption
        val algorithm: AlgorithmName = AlgorithmName.valueOf(encryption.algorithm)
        val key: String = encryption.key
        val salt: String = encryption.salt

        return when (algorithm) {
            AlgorithmName.AES_256_PBE_CBC -> Algorithms.AES_256_PBE_CBC(password = key, salt = salt)
            AlgorithmName.AES_256_PBE_GCM -> Algorithms.AES_256_PBE_GCM(password = key, salt = salt)
            AlgorithmName.BLOW_FISH -> Algorithms.BLOW_FISH(key = key)
            AlgorithmName.TRIPLE_DES -> Algorithms.TRIPLE_DES(secretKey = key)
        }
    }
}
