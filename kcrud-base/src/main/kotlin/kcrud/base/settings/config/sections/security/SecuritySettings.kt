/*
 * Copyright (c) 2024-Present Perraco. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.settings.config.sections.security

import kcrud.base.settings.config.parser.IConfigSection
import kcrud.base.settings.config.sections.security.sections.*

/**
 * Top level section for the Security related settings.
 *
 * @property isEnabled Whether to enable Basic and JWT authentication.
 * @property useSecureConnection Whether to use a secure connection or not.
 * @property encryption Settings related to encryption, such as the encryption keys.
 * @property constraints Settings related to security constraints, such endpoints rate limits.
 * @property jwt Settings related to JWT authentication, such as the JWT secrets.
 * @property basic Settings related to basic authentication, such as the realm and provider name.
 * @property oauth Settings related to OAuth authentication, such as the client id and secret.
 * @property rbac Settings related to RBAC authentication.
 */
data class SecuritySettings(
    val isEnabled: Boolean,
    val useSecureConnection: Boolean,
    val encryption: EncryptionSettings,
    val constraints: ConstraintsSettings,
    val jwt: JwtSettings,
    val basic: HttpAuthSettings,
    val oauth: OAuthSettings,
    val rbac: RbacSettings
) : IConfigSection {
    companion object {
        const val MIN_KEY_LENGTH: Int = 12
    }
}
