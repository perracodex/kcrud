/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.scheduling.routing

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kcrud.base.scheduling.entity.JobScheduleEntity
import kcrud.base.scheduling.service.JobSchedulerService
import kcrud.base.settings.AppSettings
import kcrud.base.utils.NetworkUtils
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Route displaying all scheduled jobs.
 */
fun Route.quartzRoutes() {

    authenticate(AppSettings.security.basic.providerName, optional = !AppSettings.security.isEnabled) {
        get("/scheduler") {
            val jobs: List<JobScheduleEntity> = JobSchedulerService.getJobs()

            call.respondText(
                text = Json.encodeToString(value = jobs),
                contentType = ContentType.Application.Json
            )
        }
    }

    NetworkUtils.logEndpoints(
        reason = "Scheduled Jobs",
        endpoints = listOf("scheduler")
    )
}

