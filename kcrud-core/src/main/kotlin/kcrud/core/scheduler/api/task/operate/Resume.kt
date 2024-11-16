/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.core.scheduler.api.task.operate

import io.github.perracodex.kopapi.dsl.operation.api
import io.github.perracodex.kopapi.dsl.parameter.queryParameter
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import kcrud.core.scheduler.api.SchedulerRouteApi
import kcrud.core.scheduler.model.task.TaskStateChange
import kcrud.core.scheduler.service.SchedulerService

/**
 * Resume a concrete scheduler task.
 */
@SchedulerRouteApi
internal fun Route.resumeSchedulerTaskRoute() {
    post("/admin/scheduler/task/resume") {
        val groupId: String = call.parameters.getOrFail(name = "groupId")
        val taskId: String? = call.parameters["taskId"]
        val state: TaskStateChange = SchedulerService.tasks.resume(groupId = groupId, taskId = taskId)
        call.respond(status = HttpStatusCode.OK, message = state)
    } api {
        tags = setOf("Scheduler")
        summary = "Resume a scheduler task."
        description = "Resume a concrete scheduler task."
        operationId = "resumeSchedulerTask"
        queryParameter<String>(name = "groupId") {
            description = "The group of the task."
        }
        queryParameter<String>(name = "taskId") {
            description = "The Id of the task, or null to resume all tasks in the group."
        }
        response<TaskStateChange>(status = HttpStatusCode.OK) {
            description = "The state of the task."
        }
    }
}
