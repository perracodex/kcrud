/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package krud.core.event.api

import io.ktor.server.routing.*
import io.ktor.server.sse.*
import io.ktor.sse.*
import kotlinx.io.IOException
import krud.core.env.Tracer
import krud.core.event.SseRouteApi
import krud.core.event.SseService

/**
 * Configures the Server-Sent Events (SSE) endpoint for the application.
 *
 * #### References
 * - [SSE Plugin](https://ktor.io/docs/server-server-sent-events.html)
 */
@SseRouteApi
internal fun Route.sseCallbackRoute() {
    val tracer = Tracer(ref = ::sseCallbackRoute)

    sse("/admin/events") {
        try {
            val clientId: String? = call.request.queryParameters["clientId"]
            tracer.info("Client connected, SSE channel opened. Client ID: $clientId")

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
            tracer.info("Client disconnected, SSE channel closed.")
        } catch (e: Exception) {
            tracer.error(message = "SSE connection encountered an unexpected error.", cause = e)
        }
    }
}
