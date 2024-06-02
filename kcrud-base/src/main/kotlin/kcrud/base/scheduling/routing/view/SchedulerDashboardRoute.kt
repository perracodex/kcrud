/*
 * Copyright (c) 2024-Present Perracodex. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.scheduling.routing.view

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.thymeleaf.*
import kcrud.base.scheduling.entity.JobScheduleEntity
import kcrud.base.scheduling.service.JobSchedulerService

fun Route.schedulerDashboardRoute() {
    staticResources(remotePath = "/templates", basePackage = "templates")

    get("dashboard") {
        val jobs: List<JobScheduleEntity> = JobSchedulerService.getJobs()
        val content = ThymeleafContent(template = "scheduler-dashboard", model = mapOf("data" to jobs))
        call.respond(message = content)
    }
}
