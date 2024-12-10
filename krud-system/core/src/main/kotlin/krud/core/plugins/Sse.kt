/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package krud.core.plugins

import io.ktor.server.application.*
import io.ktor.server.sse.*

/**
 * Configures SSE (Server-Sent Events) plugin.
 *
 * #### References
 * - [SSE Plugin](https://ktor.io/docs/server-server-sent-events.html)
 */
public fun Application.configureSse() {
    install(plugin = SSE)
}
