/*
 * Copyright (c) 2024-Present Perraco. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.settings.config.sections.security.sections

import kcrud.base.settings.config.parser.IConfigSection

/**
 * Configuration parameters for HTTP authentication mechanisms.
 *
 * @property providerName Name of the authentication provider.
 * @property realm Security realm for the HTTP authentication, used to differentiate between protection spaces.
 */
data class HttpAuthSettings(
    val providerName: String,
    val realm: String,
) : IConfigSection
