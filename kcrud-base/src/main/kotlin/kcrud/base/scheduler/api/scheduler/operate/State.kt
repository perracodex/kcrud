/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.scheduler.api.scheduler.operate

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kcrud.base.scheduler.api.SchedulerRouteAPI
import kcrud.base.scheduler.service.core.SchedulerService

/**
 * Returns the state of the task scheduler.
 */
@SchedulerRouteAPI
internal fun Route.schedulerStateRoute() {
    /**
     * Returns the state of the task scheduler.
     * @OpenAPITag Scheduler - Maintenance
     */
    get("scheduler/state") {
        val state: SchedulerService.TaskSchedulerState = SchedulerService.state()
        call.respond(status = HttpStatusCode.OK, message = state.name)
    }
}

