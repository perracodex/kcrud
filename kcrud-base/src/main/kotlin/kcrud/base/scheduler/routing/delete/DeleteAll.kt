/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.scheduler.routing.delete

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kcrud.base.scheduler.service.SchedulerService

/**
 * Deletes all the scheduler tasks.
 */
fun Route.deleteAllSchedulerTasksRoute() {
    // Deletes all scheduler tasks.
    delete {
        val deleteCount: Int = SchedulerService.deleteAll()
        call.respond(status = HttpStatusCode.OK, message = deleteCount)
    }
}
