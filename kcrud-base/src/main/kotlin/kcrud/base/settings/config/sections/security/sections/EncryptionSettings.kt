/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.settings.config.sections.security.sections

import kcrud.base.settings.config.parser.IConfigSection
import kcrud.base.settings.config.sections.security.SecuritySettings

/**
 * Encryption key settings.
 *
 * @property algorithm Algorithm used for encrypting/decrypting data.
 * @property salt Salt used for encrypting/decrypting data.
 * @property key Secret key for encrypting/decrypting data.
 * @property sign Signature key to sign the encrypted data.
 */
data class EncryptionSettings(
    val algorithm: String,
    val salt: String,
    val key: String,
    val sign: String
) : IConfigSection {
    init {
        require(algorithm.isNotBlank()) { "Missing encryption algorithm." }
        require(salt.isNotBlank()) { "Missing encryption salt." }
        require(key.isNotBlank() && (key.length >= SecuritySettings.MIN_KEY_LENGTH)) {
            "Invalid encryption key. Must be >= ${SecuritySettings.MIN_KEY_LENGTH} characters long."
        }
        require(sign.isNotBlank() && (sign.length >= SecuritySettings.MIN_KEY_LENGTH)) {
            "Invalid sign key. Must be >= ${SecuritySettings.MIN_KEY_LENGTH} characters long."
        }
    }
}
