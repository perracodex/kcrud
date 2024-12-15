/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package krud.base.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.hsts.*
import io.ktor.server.plugins.httpsredirect.*
import krud.base.settings.AppSettings

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
 * #### References
 * - [Ktor HTTPS Redirect Plugin](https://ktor.io/docs/server-https-redirect.html)
 * - [Ktor HSTS (HTTP Strict Transport Security) Plugin](https://ktor.io/docs/server-hsts.html)
 */
@Suppress("MagicNumber")
public fun Application.configureSecureConnection() {

    if (!AppSettings.security.useSecureConnection) {
        return
    }

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
