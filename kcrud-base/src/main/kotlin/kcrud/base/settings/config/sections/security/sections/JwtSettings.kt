/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.settings.config.sections.security.sections

import kcrud.base.settings.config.parser.IConfigSection
import kcrud.base.settings.config.sections.security.SecuritySettings

/**
 * JWT authentication settings.
 *
 * @property providerName Name of the JWT provider.
 * @property tokenLifetimeSec Authentication token lifetime, (seconds).
 * @property audience Intended recipients of the JWT.
 * @property issuer Provider that issues the JWT.
 * @property realm Security realm for the JWT authentication.
 * @property secretKey Secret key for signing the JWT.
 */
data class JwtSettings(
    val providerName: String,
    val tokenLifetimeSec: Long,
    val audience: String,
    val issuer: String,
    val realm: String,
    val secretKey: String
) : IConfigSection {
    init {
        require(providerName.isNotBlank()) { "Missing JWT provider name." }
        require(tokenLifetimeSec > 0L) { "Invalid JWT token lifetime. Must be > 0." }
        require(audience.isNotBlank()) { "Missing JWT audience." }
        require(issuer.isNotBlank()) { "Missing JWT issuer." }
        require(realm.isNotBlank()) { "Missing JWT realm." }
        require(secretKey.isNotBlank() && (secretKey.length >= SecuritySettings.MIN_KEY_LENGTH)) {
            "Invalid JWT secret key. Must be >= ${SecuritySettings.MIN_KEY_LENGTH} characters long."
        }
    }
}
