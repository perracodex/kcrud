/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.scheduler.routing.scheduler.audit

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kcrud.base.scheduler.audit.AuditService
import kcrud.base.scheduler.model.audit.AuditLog

/**
 * Returns all existing audit logs for the scheduler.
 */
internal fun Route.schedulerAllAuditRoute() {
    // Returns all existing audit logs for the scheduler.
    get("scheduler/audit") {
        val audit: List<AuditLog> = AuditService.findAll()
        call.respond(status = HttpStatusCode.OK, message = audit)
    }
}
