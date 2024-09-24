/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.core.events

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Configures the Server-Sent Events (SSE) endpoint for the application.
 *
 * This route handles SSE connections, setting the appropriate headers
 * for content type and cache control to ensure real-time updates are
 * sent to the client.
 */
public fun Route.sseRoute() {
    /**
     * Server-Sent Events (SSE) endpoint.
     * @OpenAPITag System
     */
    get("/events") {
        call.response.header(HttpHeaders.ContentType, ContentType.Text.EventStream.toString())
        call.response.cacheControl(CacheControl.NoCache(visibility = null))

        call.respondTextWriter(ContentType.Text.EventStream) {
            SEEService.write(writer = this)
        }
    }
}
