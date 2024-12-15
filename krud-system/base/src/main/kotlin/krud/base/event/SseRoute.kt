/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package krud.base.event

import io.ktor.server.routing.*
import krud.base.event.api.sseCallbackRoute
import krud.base.event.api.sseClearRoute
import krud.base.event.api.sseGetAllRoute

/**
 * Configures the Server-Sent Events (SSE) endpoint for the application.
 *
 * #### References
 * - [SSE Plugin](https://ktor.io/docs/server-server-sent-events.html)
 */
@OptIn(SseRouteApi::class)
public fun Route.sseRoutes() {
    sseCallbackRoute()
    sseClearRoute()
    sseGetAllRoute()
}

/**
 * Annotation for controlled access to the SSE Routes API.
 */
@RequiresOptIn(level = RequiresOptIn.Level.ERROR, message = "Only to be used within the SSE Routes API.")
@Retention(AnnotationRetention.BINARY)
internal annotation class SseRouteApi
