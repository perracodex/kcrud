/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.core.scheduler.api.scheduler.operate

import io.github.perracodex.kopapi.dsl.operation.api
import io.github.perracodex.kopapi.dsl.parameter.queryParameter
import io.github.perracodex.kopapi.type.DefaultValue
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
    post("/admin/scheduler/restart/{interrupt?}") {
        val interrupt: Boolean = call.queryParameters["interrupt"].toBoolean()
        val state: SchedulerService.TaskSchedulerState = SchedulerService.restart(interrupt = interrupt)
        call.respond(status = HttpStatusCode.OK, message = state.name)
    } api {
        tags = setOf("Scheduler Admin")
        summary = "Restart the task scheduler."
        description = "Restart the task scheduler."
        operationId = "restartScheduler"
        queryParameter<Boolean>(name = "interrupt") {
            description = "Whether to interrupt the current tasks."
            required = false
            defaultValue = DefaultValue.ofBoolean(value = false)
        }
        response<String>(status = HttpStatusCode.OK) {
            description = "The state of the scheduler."
        }
    }
}
