/*
 * Copyright (c) 2023-Present Perraco. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.routing.*

/**
 * Configures Http related plugins.
 *
 * See: [Compression Plugin](https://ktor.io/docs/server-compression.html)
 */
fun Application.configureHttp() {

    // Provides the capability to compress outgoing content.
    // You can use different compression algorithms, including gzip and deflate,
    // specify the required conditions for compressing data, such as a content type
    // or response size, or even compress data based on specific request parameters.
    install(plugin = Compression)

    // Ignore trailing slashes in routes,
    // so that "http://example.com", "http://example.com/" "http://example.com/#" are treated as the same.
    install(plugin = IgnoreTrailingSlash)
}
