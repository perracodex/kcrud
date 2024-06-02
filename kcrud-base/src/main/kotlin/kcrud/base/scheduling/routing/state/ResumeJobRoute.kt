/*
 * Copyright (c) 2024-Present Perracodex. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.scheduling.routing.state

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kcrud.base.scheduling.service.JobSchedulerService

/**
 * Resume a concrete scheduled job.
 */
fun Route.resumeScheduledJobRoute() {
    route("{name}/{group}/resume") {
        // Resume a concrete scheduled job.
        post {
            val name: String = call.parameters["name"]!!
            val group: String = call.parameters["group"]!!
            val success: Boolean = JobSchedulerService.resumeJob(name = name, group = group)

            call.respond(
                status = if (success) HttpStatusCode.OK else HttpStatusCode.NotFound,
                message = success
            )
        }
    }
}
