/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.scheduler.routing.scheduler

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kcrud.base.scheduler.model.TaskStateChangeDto
import kcrud.base.scheduler.service.core.SchedulerService

/**
 * Resume all the scheduler tasks.
 */
internal fun Route.resumeSchedulerRoute() {
    // Resume all the scheduler tasks.
    post("scheduler/resume") {
        val state: TaskStateChangeDto = SchedulerService.resume()
        call.respond(status = HttpStatusCode.OK, message = state)
    }
}
