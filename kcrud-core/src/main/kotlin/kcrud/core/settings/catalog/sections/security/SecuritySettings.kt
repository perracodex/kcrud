/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.core.settings.catalog.sections.security

import kcrud.core.settings.catalog.sections.security.sections.ConstraintsSettings
import kcrud.core.settings.catalog.sections.security.sections.EncryptionSettings
import kcrud.core.settings.catalog.sections.security.sections.RbacSettings
import kcrud.core.settings.catalog.sections.security.sections.auth.BasicAuthSettings
import kcrud.core.settings.catalog.sections.security.sections.auth.JwtAuthSettings
import kcrud.core.settings.catalog.sections.security.sections.auth.OAuthSettings
import kcrud.core.settings.parser.IConfigCatalogSection
import kotlinx.serialization.Serializable

/**
 * Top level section for the Security related settings.
 *
 * @property isEnabled Whether to enable Basic and JWT authentication.
 * @property useSecureConnection Whether to use a secure connection or not.
 * @property encryption Settings related to encryption, such as the encryption keys.
 * @property constraints Settings related to security constraints, such endpoints rate limits.
 * @property basicAuth Settings related to basic authentication, such as the realm and provider name.
 * @property jwtAuth Settings related to JWT authentication, such as the JWT secrets.
 * @property oAuth Settings related to OAuth authentication, such as the client id and secret.
 * @property rbac Settings related to RBAC authentication.
 */
@Serializable
public data class SecuritySettings(
    val isEnabled: Boolean,
    val useSecureConnection: Boolean,
    val encryption: EncryptionSettings,
    val constraints: ConstraintsSettings,
    val basicAuth: BasicAuthSettings,
    val jwtAuth: JwtAuthSettings,
    val oAuth: OAuthSettings,
    val rbac: RbacSettings
) : IConfigCatalogSection {
    public companion object {
        /** The minimum length for a security key, such as encryption and secret keys. */
        public const val MIN_KEY_LENGTH: Int = 12
    }
}
