/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.plugins

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.cachingheaders.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.forwardedheaders.*

/**
 * Configures header related plugins.
 *
 * See: [Default Headers Documentation](https://ktor.io/docs/server-default-headers.html)
 *
 * See: [Auto Head Response Documentation](https://ktor.io/docs/server-autoheadresponse.html)
 *
 * See: [Caching Headers Plugin](https://ktor.io/docs/server-caching-headers.html)
 *
 * See: [Forwarded-Header Plugin](https://ktor.io/docs/server-forward-headers.html)
 */
fun Application.configureHeaders() {

    // Provides with the ability to automatically respond to a HEAD request
    // for every route that has a GET defined.
    // Can use AutoHeadResponse to avoid creating a separate head handler
    // if needing to somehow process a response on the client before getting
    // the actual content. For example, calling the respondFile function adds
    // the Content-Length and Content-Type headers to a response automatically,
    // and then can get this information on the client before downloading the file.
    install(plugin = AutoHeadResponse)

    // Adds the standard Server and Date headers into each response.
    // Moreover, is possible to provide additional default headers and override the Server header.
    install(plugin = DefaultHeaders) {
        header(name = "X-Engine", value = "Kcrud")
    }

    // The CachingHeaders plugin adds the capability to configure
    // the Cache-Control and Expires headers used for HTTP caching.
    install(plugin = CachingHeaders) {
        options { _, content ->
            when (content.contentType?.withoutParameters()) {
                ContentType.Text.Plain -> CachingOptions(CacheControl.MaxAge(maxAgeSeconds = 0))
                ContentType.Text.Html -> CachingOptions(CacheControl.MaxAge(maxAgeSeconds = 0))
                else -> null
            }
        }
    }

    // The ForwardedHeaders and XForwardedHeaders plugins allow to handle
    // reverse proxy headers to get information about the original request
    // when a Ktor server is placed behind a reverse proxy.
    // This might be useful for logging purposes.
    // WARNING: Should be only used when the server is behind a reverse proxy.
    install(plugin = XForwardedHeaders)
    // install(plugin = ForwardedHeaders)
}
