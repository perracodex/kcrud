/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.core.scheduler.api.scheduler.operate

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kcrud.core.scheduler.api.SchedulerRouteAPI
import kcrud.core.scheduler.model.task.TaskStateChange
import kcrud.core.scheduler.service.SchedulerService

/**
 * Pauses all the scheduler tasks.
 */
@SchedulerRouteAPI
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
