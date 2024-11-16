/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.core.scheduler.api.task.fetch

import io.github.perracodex.kopapi.dsl.operation.api
import io.github.perracodex.kopapi.dsl.parameter.queryParameter
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kcrud.core.persistence.util.toUuidOrNull
import kcrud.core.scheduler.api.SchedulerRouteApi
import kcrud.core.scheduler.model.task.TaskSchedule
import kcrud.core.scheduler.service.SchedulerService
import kotlin.uuid.Uuid

/**
 * Gets all scheduler tasks.
 */
@SchedulerRouteApi
internal fun Route.getSchedulerTasksRoute() {
    get("/admin/scheduler/task") {
        val groupId: Uuid? = call.queryParameters["groupId"]?.toUuidOrNull()
        val tasks: List<TaskSchedule> = SchedulerService.tasks.all(groupId = groupId)
        call.respond(status = HttpStatusCode.OK, message = tasks)
    } api {
        tags = setOf("Scheduler")
        summary = "Get all scheduler tasks."
        description = "Get all the scheduler tasks."
        operationId = "getSchedulerTasks"
        queryParameter<Uuid>(name = "groupId") {
            description = "The group ID of the tasks."
        }
        response<List<TaskSchedule>>(status = HttpStatusCode.OK) {
            description = "The list of tasks."
        }
    }
}
