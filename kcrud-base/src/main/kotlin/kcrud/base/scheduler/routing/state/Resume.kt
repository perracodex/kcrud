/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.scheduler.routing.state

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kcrud.base.scheduler.service.SchedulerService

/**
 * Resume a concrete scheduler task.
 */
fun Route.resumeSchedulerTaskRoute() {
    route("{name}/{group}/resume") {
        // Resume a concrete scheduled task.
        post {
            val name: String = call.parameters["name"]!!
            val group: String = call.parameters["group"]!!
            val success: Boolean = SchedulerService.resumeTask(name = name, group = group)

            call.respond(
                status = if (success) HttpStatusCode.OK else HttpStatusCode.NotFound,
                message = success
            )
        }
    }
}
