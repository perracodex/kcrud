/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.scheduler.routing.scheduler

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kcrud.base.scheduler.audit.AuditRepository
import kcrud.base.scheduler.audit.entity.AuditEntity

/**
 * Returns the scheduler audit routes.
 */
fun Route.schedulerAuditRoute() {
    // Returns the audit logs for the scheduler.
    get("audit") {
        val audit: List<AuditEntity> = AuditRepository.findAll()
        call.respond(status = HttpStatusCode.OK, message = audit)
    }

    // Returns the audit log for a specific task.
    get("audit/{name}/{group}") {
        val taskName: String = call.parameters["name"] ?: return@get call.respond(HttpStatusCode.BadRequest)
        val taskGroup: String = call.parameters["group"] ?: return@get call.respond(HttpStatusCode.BadRequest)
        val audit: AuditEntity? = AuditRepository.find(taskName = taskName, taskGroup = taskGroup)

        audit?.let {
            call.respond(status = HttpStatusCode.OK, message = audit)
        } ?: call.respond(HttpStatusCode.NotFound)
    }
}
