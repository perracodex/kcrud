/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.core.scheduler.api.scheduler.operate

import io.github.perracodex.kopapi.dsl.operation.api
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kcrud.core.scheduler.api.SchedulerRouteApi
import kcrud.core.scheduler.service.SchedulerService

/**
 * Returns the state of the task scheduler.
 */
@SchedulerRouteApi
internal fun Route.schedulerStateRoute() {
    get("/admin/scheduler/state") {
        val state: SchedulerService.TaskSchedulerState = SchedulerService.state()
        call.respond(status = HttpStatusCode.OK, message = state.name)
    } api {
        tags = setOf("Scheduler Admin")
        summary = "Get the state of the task scheduler."
        description = "Get the state of the task scheduler."
        operationId = "getSchedulerState"
        response<String>(status = HttpStatusCode.OK) {
            description = "The state of the scheduler."
        }
    }
}

