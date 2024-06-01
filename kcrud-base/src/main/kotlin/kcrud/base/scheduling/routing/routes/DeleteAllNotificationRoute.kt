/*
 * Copyright (c) 2024-Present Perracodex. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.scheduling.routing.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kcrud.base.scheduling.service.JobSchedulerService

/**
 * Deletes all scheduled notifications.
 */
fun Route.deleteAllNotificationRoute() {
    // Delete all scheduled notifications.
    delete {
        val jobs: Int = JobSchedulerService.deleteAll()

        call.respond(
            status = HttpStatusCode.OK,
            message = "Jobs deleted: $jobs"
        )
    }
}
