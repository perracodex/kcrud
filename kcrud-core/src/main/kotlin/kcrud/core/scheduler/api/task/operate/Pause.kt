/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.core.scheduler.api.task.operate

import io.github.perracodex.kopapi.dsl.operation.api
import io.github.perracodex.kopapi.dsl.parameter.pathParameter
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import kcrud.core.scheduler.api.SchedulerRouteApi
import kcrud.core.scheduler.model.task.TaskStateChange
import kcrud.core.scheduler.service.SchedulerService

/**
 * Pause a concrete scheduler task.
 */
@SchedulerRouteApi
internal fun Route.pauseSchedulerTaskRoute() {
    post("/admin/scheduler/task/{name}/{group}/pause") {
        val name: String = call.parameters.getOrFail(name = "name")
        val group: String = call.parameters.getOrFail(name = "group")
        val state: TaskStateChange = SchedulerService.tasks.pause(name = name, group = group)
        call.respond(status = HttpStatusCode.OK, message = state)
    } api {
        tags = setOf("Scheduler")
        summary = "Pause a scheduler task."
        description = "Pause a concrete scheduler task."
        operationId = "pauseSchedulerTask"
        pathParameter<String>(name = "name") {
            description = "The name of the task."
        }
        pathParameter<String>(name = "group") {
            description = "The group of the task."
        }
        response<TaskStateChange>(status = HttpStatusCode.OK) {
            description = "The state of the task."
        }
    }
}
