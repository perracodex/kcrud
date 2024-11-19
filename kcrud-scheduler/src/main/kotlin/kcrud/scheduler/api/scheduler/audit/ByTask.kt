/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.scheduler.api.scheduler.audit

import io.github.perracodex.kopapi.dsl.operation.api
import io.github.perracodex.kopapi.dsl.parameter.queryParameter
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.perracodex.exposed.pagination.Page
import io.perracodex.exposed.pagination.getPageable
import kcrud.core.persistence.util.toUuidOrNull
import kcrud.core.plugins.Uuid
import kcrud.core.util.trimOrNull
import kcrud.scheduler.audit.AuditService
import kcrud.scheduler.model.audit.AuditLog

/**
 * Returns the audit log for a specific task.
 */
internal fun Route.schedulerAuditByTaskRoute() {
    get("/admin/scheduler/audit/task") {
        val groupId: Uuid? = call.queryParameters["groupId"].toUuidOrNull()
        val taskId: String? = call.queryParameters["taskId"].trimOrNull()

        if (groupId == null && taskId.isNullOrBlank()) {
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
        queryParameter<Uuid>(name = "groupId") {
            description = "The group of the task."
            required = false
        }
        queryParameter<String>(name = "taskId") {
            description = "The unique identifier of the task."
            required = false
        }
        response<Page<AuditLog>>(status = HttpStatusCode.OK) {
            description = "Existing scheduler audit logs for the task."
        }
        response(status = HttpStatusCode.BadRequest) {
            description = "Missing required parameters. At least one of 'groupId' and 'taskId' is required."
        }
    }
}
