/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.core.settings.catalog.sections.security.nodes.auth

import io.github.perracodex.ktor.config.IConfigCatalogSection
import kcrud.core.settings.catalog.sections.security.SecuritySettings
import kotlinx.serialization.Serializable

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
@Serializable
public data class JwtAuthSettings(
    val providerName: String,
    val tokenLifetimeSec: Long,
    val audience: String,
    val issuer: String,
    val realm: String,
    val secretKey: String
) : IConfigCatalogSection {
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
