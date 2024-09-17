/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.scheduler.api.scheduler.operate

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kcrud.base.scheduler.service.core.SchedulerService

/**
 * Restart the task scheduler.
 */
internal fun Route.restartSchedulerRoute() {
    /**
     * Restart the task scheduler.
     * @OpenAPITag Scheduler - Maintenance
     */
    post("scheduler/restart") {
        val interrupt: Boolean = call.parameters["interrupt"]?.toBoolean() ?: false
        val state: SchedulerService.TaskSchedulerState = SchedulerService.restart(interrupt = interrupt)
        call.respond(status = HttpStatusCode.OK, message = state.name)
    }
}
