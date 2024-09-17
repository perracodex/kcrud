/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.scheduler.api.scheduler.operate

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kcrud.base.scheduler.model.task.TaskStateChange
import kcrud.base.scheduler.service.core.SchedulerService

/**
 * Pauses all the scheduler tasks.
 */
internal fun Route.pauseSchedulerRoute() {
    /**
     * Pauses all the scheduler tasks.
     * @OpenAPITag Scheduler - Maintenance
     */
    post("scheduler/pause") {
        val state: TaskStateChange = SchedulerService.pause()
        call.respond(status = HttpStatusCode.OK, message = state)
    }
}
