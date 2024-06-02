/*
 * Copyright (c) 2024-Present Perracodex. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.scheduling.routing.get

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kcrud.base.scheduling.service.JobSchedulerService
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Gets all scheduled job groups.
 */
fun Route.getScheduledJobGroupRoute() {
    // Gets all scheduled job groups.
    get("/groups") {
        val groups: List<String> = JobSchedulerService.getGroups()

        call.respondText(
            text = Json.encodeToString(value = groups),
            contentType = ContentType.Application.Json
        )
    }
}
