/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.scheduler.routing.state

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kcrud.base.scheduler.entity.TaskStateChangeEntity
import kcrud.base.scheduler.service.core.SchedulerService

/**
 * Pauses all the scheduler tasks.
 */
fun Route.pauseAllSchedulerTasksRoute() {
    // Pauses all the scheduler tasks.
    post("/pause") {
        val state: TaskStateChangeEntity = SchedulerService.pause()
        call.respond(status = HttpStatusCode.OK, message = state)
    }
}
