/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.scheduler.api.task.operate

import io.github.perracodex.kopapi.dsl.operation.api
import io.github.perracodex.kopapi.dsl.parameter.queryParameter
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import kcrud.core.plugins.Uuid
import kcrud.core.util.toUuid
import kcrud.core.util.trimOrNull
import kcrud.scheduler.model.task.TaskStateChange
import kcrud.scheduler.service.SchedulerService

/**
 * Pause a concrete scheduler task.
 */
internal fun Route.pauseSchedulerTaskRoute() {
    post("/admin/scheduler/task/pause") {
        val groupId: Uuid = call.parameters.getOrFail(name = "groupId").toUuid()
        val taskId: String? = call.parameters["taskId"].trimOrNull()
        val state: TaskStateChange = SchedulerService.tasks.pause(groupId = groupId, taskId = taskId)
        call.respond(status = HttpStatusCode.OK, message = state)
    } api {
        tags = setOf("Scheduler")
        summary = "Pause a scheduler task."
        description = "Pause a concrete scheduler task."
        operationId = "pauseSchedulerTask"
        queryParameter<Uuid>(name = "groupId") {
            description = "The group of the task."
        }
        queryParameter<String>(name = "taskId") {
            description = "The Id of the task, or null to pause all tasks in the group."
            required = false
        }
        response<TaskStateChange>(status = HttpStatusCode.OK) {
            description = "The state of the task."
        }
    }
}
