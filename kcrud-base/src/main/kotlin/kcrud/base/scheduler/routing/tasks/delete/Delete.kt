/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.scheduler.routing.tasks.delete

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kcrud.base.scheduler.service.core.SchedulerService

/**
 * Deletes a concrete scheduler task.
 */
internal fun Route.deleteSchedulerTaskRoute() {
    // Deletes a concrete scheduler task.
    delete("{name}/{group}") {
        val name: String = call.parameters["name"]!!
        val group: String = call.parameters["group"]!!
        val deletedCount: Int = SchedulerService.tasks.delete(name = name, group = group)
        call.respond(status = HttpStatusCode.OK, message = deletedCount)
    }
}
