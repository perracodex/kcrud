/*
 * Copyright (c) 2023-Present Perraco. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.hsts.*
import io.ktor.server.plugins.httpsredirect.*
import kcrud.base.settings.AppSettings

/**
 * Configures the secure connection for the application.
 *
 * The [HttpsRedirect] plugin redirects all HTTP requests to the HTTPS counterpart
 * before processing the call.
 * By default, a resource returns 301 Moved Permanently, but it can be configured to be 302 Found.
 *
 * The [HSTS] plugin adds the required HTTP Strict Transport Security headers to the request according
 * to the RFC 6797. When the browser receives HSTS policy headers, it no longer attempts to connect
 * to the server with insecure connections for a given period.
 *
 * See: [Ktor HTTPS Redirect Plugin](https://ktor.io/docs/https-redirect.html)
 *
 * See: [Ktor HSTS (HTTP Strict Transport Security) Plugin](https://ktor.io/docs/hsts.html)
 */
fun Application.configureSecureConnection() {

    if (!AppSettings.security.useSecureConnection)
        return

    // The HttpsRedirect plugin redirects all HTTP requests to the HTTPS counterpart
    // before processing the call. By default, a resource returns 301 Moved Permanently,
    // but it can be configured to be 302 Found.
    install(plugin = HttpsRedirect) {
        sslPort = AppSettings.deployment.sslPort
    }

    // The HSTS plugin adds the required HTTP Strict Transport Security headers
    // to the request according to the RFC 6797. When the browser receives
    // HSTS policy headers, it no longer attempts to connect to the server
    // with insecure connections for a given period.
    install(plugin = HSTS) {
        maxAgeInSeconds = 10
        includeSubDomains = true
    }
}
