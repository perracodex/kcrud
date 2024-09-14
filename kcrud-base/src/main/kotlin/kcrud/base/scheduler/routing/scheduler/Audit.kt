/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.scheduler.routing.scheduler

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import kcrud.base.scheduler.audit.AuditService
import kcrud.base.scheduler.model.audit.AuditLog

/**
 * Returns the scheduler audit routes.
 */
internal fun Route.schedulerAuditRoute() {
    // Returns the audit logs for the scheduler.
    get("scheduler/audit") {
        val audit: List<AuditLog> = AuditService.findAll()
        call.respond(status = HttpStatusCode.OK, message = audit)
    }

    // Returns the audit log for a specific task.
    get("scheduler/audit/{name}/{group}") {
        val taskName: String = call.parameters.getOrFail(name = "name")
        val taskGroup: String = call.parameters.getOrFail(name = "group")
        val audit: List<AuditLog> = AuditService.find(taskName = taskName, taskGroup = taskGroup)

        if (audit.isEmpty()) {
            call.respond(status = HttpStatusCode.NotFound, message = "No audit logs found for the task.")
        } else {
            call.respond(status = HttpStatusCode.OK, message = audit)
        }
    }
}
