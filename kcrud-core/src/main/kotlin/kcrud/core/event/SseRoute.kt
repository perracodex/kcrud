/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.core.event

import io.ktor.server.routing.*
import io.ktor.server.sse.*
import io.ktor.sse.*
import kcrud.core.env.Tracer
import kotlinx.io.IOException

/**
 * Configures the Server-Sent Events (SSE) endpoint for the application.
 *
 * #### References
 * - [SSE Plugin](https://ktor.io/docs/server-server-sent-events.html)
 */
public fun Route.sseRoute() {
    /**
     * Server-Sent Events (SSE) endpoint.
     * @OpenAPITag System
     */
    sse("/admin/events") {
        try {
            SseService.eventFlow.collect { message ->
                val event = ServerSentEvent(data = message)
                send(event = event)
            }
        } catch (_: IOException) {
            // This exception often occurs when the client disconnects mid-stream, such as:
            // - When the client closes the browser tab, refreshes or navigates away.
            // - When the network connection is lost (e.g., going offline).
            // - When the client intentionally closes the SSE connection.
            //
            // The SSE channel closes, which stops further event transmission.
            // This is a normal, harmless occurrence and does not require additional handling,
            // as each client manages its own reconnection attempts based on the browser's EventSource configuration.
            Tracer(ref = ::sseRoute).info("Client disconnected, SSE channel closed.")
        } catch (e: Exception) {
            Tracer(ref = ::sseRoute).error(message = "SSE connection encountered an unexpected error.", cause = e)
        }
    }
}
