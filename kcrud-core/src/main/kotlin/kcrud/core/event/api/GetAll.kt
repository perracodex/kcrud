/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.core.event.api

import io.github.perracodex.kopapi.dsl.operation.api
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kcrud.core.event.SseRouteApi
import kcrud.core.event.SseService

/**
 * Returns all the system events.
 */
@SseRouteApi
internal fun Route.sseGetAllRoute() {
    get("/admin/events/all") {
        val events: List<String> = SseService.getAllEvents()
        call.respond(status = HttpStatusCode.OK, message = events)
    } api {
        tags = setOf("Events")
        summary = "Get all the events."
        description = "Get all the system events."
        operationId = "getAllEvents"
        response<List<String>>(status = HttpStatusCode.OK) {
            description = "The events are returned."
        }
    }
}
