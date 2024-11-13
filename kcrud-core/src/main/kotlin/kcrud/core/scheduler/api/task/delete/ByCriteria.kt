/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.core.scheduler.api.task.delete

import io.github.perracodex.kopapi.dsl.operation.api
import io.github.perracodex.kopapi.dsl.parameter.pathParameter
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import kcrud.core.scheduler.api.SchedulerRouteApi
import kcrud.core.scheduler.service.SchedulerService

/**
 * Deletes a concrete scheduler task.
 */
@SchedulerRouteApi
internal fun Route.deleteSchedulerTaskRoute() {
    delete("/admin/scheduler/task/{name}/{group}") {
        val name: String = call.parameters.getOrFail(name = "name")
        val group: String = call.parameters.getOrFail(name = "group")
        val deletedCount: Int = SchedulerService.tasks.delete(name = name, group = group)
        call.respond(status = HttpStatusCode.OK, message = deletedCount)
    } api {
        tags = setOf("Scheduler")
        summary = "Delete a scheduler task."
        description = "Delete a concrete scheduler task."
        operationId = "deleteSchedulerTask"
        pathParameter<String>(name = "name") {
            description = "The name of the task."
        }
        pathParameter<String>(name = "group") {
            description = "The group of the task."
        }
        response<Int>(status = HttpStatusCode.OK) {
            description = "The number of tasks deleted."
        }
    }
}
