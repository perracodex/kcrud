/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.scheduler.routing.scheduler

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kcrud.base.scheduler.service.core.SchedulerService

/**
 * Restart the task scheduler.
 */
fun Route.restartSchedulerRoute() {
    // Restart the task scheduler.
    post("restart") {
        val state: SchedulerService.TaskSchedulerState = SchedulerService.restart()
        call.respond(status = HttpStatusCode.OK, message = state.name)
    }
}
