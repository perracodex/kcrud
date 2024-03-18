/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.settings.config.sections.security.sections

import kcrud.base.settings.config.parser.IConfigSection

/**
 * OAuth-authentication settings.
 *
 * @property providerName Name of the OAuth provider.
 * @property redirectCallbackUrl Redirect URL that is opened when authorization is completed.
 * @property authorizeUrl OAuth server authorization page URL.
 * @property accessTokenUrl OAuth server access token request domain URL.
 * @property clientId Client id parameter (provided by OAuth server vendor).
 * @property clientSecret client secret parameter (provided by OAuth server vendor).
 * @property defaultScopes List of OAuth scopes used by default.
 */
data class OAuthSettings(
    val providerName: String,
    val redirectCallbackUrl: String,
    val authorizeUrl: String,
    val accessTokenUrl: String,
    val clientId: String,
    val clientSecret: String,
    val defaultScopes: List<String>
) : IConfigSection {
    init {
        require(providerName.isNotBlank()) { "Missing OAuth provider name." }
        require(redirectCallbackUrl.isNotBlank()) { "Missing OAuth redirect callback Url." }
        require(authorizeUrl.isNotBlank()) { "Missing OAuth authorize Url." }
        require(accessTokenUrl.isNotBlank()) { "Missing OAuth token Url." }
        require(clientId.isNotBlank()) { "Missing OAuth client Id." }
        require(clientSecret.isNotBlank()) { "Missing OAuth client secret." }
        require(defaultScopes.isNotEmpty()) { "Missing OAuth default scopes." }
    }
}
