/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.core.scheduler.api.scheduler.audit

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import kcrud.core.scheduler.api.SchedulerRouteApi
import kcrud.core.scheduler.audit.AuditService
import kcrud.core.scheduler.model.audit.AuditLog

/**
 * Returns the audit log for a specific task.
 */
@SchedulerRouteApi
internal fun Route.schedulerAuditByTaskRoute() {
    /**
     * Returns the audit log for a specific task.
     * @OpenAPITag Scheduler - Maintenance
     */
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
