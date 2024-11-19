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
import kcrud.core.persistence.util.toUuid
import kcrud.core.plugins.Uuid
import kcrud.core.util.trimOrNull
import kcrud.scheduler.service.SchedulerService

/**
 * Resends a concrete scheduler task.
 */
internal fun Route.resendSchedulerTaskRoute() {
    post("/admin/scheduler/task/resend") {
        val groupId: Uuid = call.parameters.getOrFail(name = "groupId").toUuid()
        val taskId: String? = call.parameters["taskId"].trimOrNull()

        if (taskId.isNullOrBlank()) {
            SchedulerService.tasks.resend(groupId = groupId)
        } else {
            SchedulerService.tasks.resend(groupId = groupId, taskId = taskId)
        }

        call.respond(HttpStatusCode.OK)
    } api {
        tags = setOf("Scheduler")
        summary = "Resend a scheduler task."
        description = "Resend a concrete scheduler task."
        operationId = "resendSchedulerTask"
        queryParameter<Uuid>(name = "groupId") {
            description = "The group of the task."
        }
        queryParameter<String>(name = "taskId") {
            description = "The Id of the task, or null to resend all tasks in the group."
            required = false
        }
        response<Unit>(status = HttpStatusCode.OK) {
            description = "The task has been resent."
        }
    }
}
