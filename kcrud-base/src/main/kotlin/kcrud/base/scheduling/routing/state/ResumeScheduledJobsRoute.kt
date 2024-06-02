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
import kcrud.base.scheduling.entity.JobScheduleStateChangeEntity
import kcrud.base.scheduling.service.JobSchedulerService

/**
 * Resume all scheduled jobs.
 */
fun Route.resumeScheduledJobsRoute() {
    // Resume all scheduled jobs.
    post("/resume") {
        val state: JobScheduleStateChangeEntity = JobSchedulerService.resume()
        call.respond(status = HttpStatusCode.OK, message = state)
    }
}
