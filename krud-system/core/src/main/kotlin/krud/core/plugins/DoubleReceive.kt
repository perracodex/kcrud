/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package krud.core.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.doublereceive.*
import io.ktor.server.request.*
import krud.core.env.Tracer
import krud.core.settings.AppSettings

/**
 * The [DoubleReceive] plugin provides the ability to receive a request body several times
 * without encountering a [RequestAlreadyConsumedException]. This capability is especially beneficial
 * in scenarios where the request body needs to be inspected, logged, or processed in multiple ways
 * before final processing. Benefits of using DoubleReceive include:
 * - Enhanced flexibility in request handling, enabling scenarios like logging and then processing the request body.
 * - Improved debugging and auditing capabilities by allowing request bodies to be inspected multiple times.
 * - Facilitation of complex application logic that requires the request to be read in different formats or multiple times.
 * - Compatibility with other plugins, such as CallLogging, to enrich application monitoring and logging.
 *
 * Downsides of using DoubleReceive include increased memory usage due to caching request bodies.
 * Additionally, careful consideration is needed to ensure that when receiving sensitive data, this is
 * received in a secure format so is not inadvertently cached in a way that could pose a security risk.
 *
 * #### References
 * - [DoubleReceive](https://ktor.io/docs/server-double-receive.html).
 */
@Suppress("MagicNumber")
public fun Application.configureDoubleReceive() {
    if (!AppSettings.runtime.doubleReceiveEnvironments.contains(AppSettings.runtime.environment)) {
        return
    }

    Tracer(ref = Application::configureCors).debug("Configuring DoubleReceive plugin.")

    install(plugin = DoubleReceive) {
        cacheRawRequest = true

        // Exclude requests larger than 2 MB from cache.
        maxSize(limit = 2 * 1024 * 1024)

        // Use file cache for requests larger than 1 MB.
        useFileForCache { call ->
            (call.request.contentLength() ?: 0) > 1 * 1024 * 1024 // 1 MB.
        }
    }
}
