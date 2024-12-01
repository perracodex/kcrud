/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.scheduler.api.task.fetch

import io.github.perracodex.kopapi.dsl.operation.api
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kcrud.scheduler.model.task.TaskGroup
import kcrud.scheduler.service.SchedulerService

/**
 * Gets all scheduler task groups.
 */
internal fun Route.getSchedulerAllGroupsRoute() {
    get("/admin/scheduler/group") {
        val groups: List<TaskGroup> = SchedulerService.tasks.groups()
        call.respond(status = HttpStatusCode.OK, message = groups)
    } api {
        tags = setOf("Scheduler")
        summary = "Get all scheduler task groups."
        description = "Get all the scheduler task groups."
        operationId = "getSchedulerTaskGroups"
        response<List<TaskGroup>>(status = HttpStatusCode.OK) {
            description = "The list of task groups."
        }
    }
}
