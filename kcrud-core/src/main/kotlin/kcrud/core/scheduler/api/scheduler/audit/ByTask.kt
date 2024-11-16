/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.core.scheduler.api.scheduler.audit

import io.github.perracodex.kopapi.dsl.operation.api
import io.github.perracodex.kopapi.dsl.parameter.queryParameter
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.perracodex.exposed.pagination.Page
import io.perracodex.exposed.pagination.getPageable
import kcrud.core.scheduler.api.SchedulerRouteApi
import kcrud.core.scheduler.audit.AuditService
import kcrud.core.scheduler.model.audit.AuditLog

/**
 * Returns the audit log for a specific task.
 */
@SchedulerRouteApi
internal fun Route.schedulerAuditByTaskRoute() {
    get("/admin/scheduler/audit/task") {
        val groupId: String? = call.queryParameters["groupId"]
        val taskId: String? = call.queryParameters["taskId"]

        if (groupId.isNullOrBlank() && taskId.isNullOrBlank()) {
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = "Missing required parameters. At least one of 'groupId' and 'taskId' is required."
            )
            return@get
        }

        val audit: Page<AuditLog> = AuditService.find(
            pageable = call.getPageable(),
            groupId = groupId,
            taskId = taskId
        )
        call.respond(status = HttpStatusCode.OK, message = audit)
    } api {
        tags = setOf("Scheduler Admin")
        summary = "Get audit logs for a specific task."
        description = "Get all existing audit logs for a specific task."
        operationId = "getAuditLogsByTask"
        queryParameter<String>(name = "groupId") {
            description = "The group of the task."
        }
        queryParameter<String>(name = "taskId") {
            description = "The unique identifier of the task."
        }
        response<Page<AuditLog>>(status = HttpStatusCode.OK) {
            description = "Existing scheduler audit logs for the task."
        }
        response(status = HttpStatusCode.BadRequest) {
            description = "Missing required parameters. At least one of 'groupId' and 'taskId' is required."
        }
    }
}
