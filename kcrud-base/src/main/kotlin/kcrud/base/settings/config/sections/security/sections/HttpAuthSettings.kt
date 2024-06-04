/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.settings.config.sections.security.sections

import kcrud.base.settings.config.parser.IConfigSection
import kotlinx.serialization.Serializable

/**
 * Configuration parameters for HTTP authentication mechanisms.
 *
 * @property providerName Name of the authentication provider.
 * @property realm Security realm for the HTTP authentication, used to differentiate between protection spaces.
 */
@Serializable
data class HttpAuthSettings(
    val providerName: String,
    val realm: String,
) : IConfigSection
