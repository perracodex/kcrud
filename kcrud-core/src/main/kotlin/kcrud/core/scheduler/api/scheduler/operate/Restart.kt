/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.core.scheduler.api.scheduler.operate

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kcrud.core.scheduler.api.SchedulerRouteApi
import kcrud.core.scheduler.service.SchedulerService

/**
 * Restart the task scheduler.
 */
@SchedulerRouteApi
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
