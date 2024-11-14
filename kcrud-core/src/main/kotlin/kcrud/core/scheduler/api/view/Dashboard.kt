/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.core.scheduler.api.view

import io.github.perracodex.kopapi.dsl.operation.api
import io.github.perracodex.kopapi.dsl.parameter.queryParameter
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.thymeleaf.*
import kcrud.core.persistence.util.toUuidOrNull
import kcrud.core.scheduler.api.SchedulerRouteApi
import kcrud.core.scheduler.model.task.TaskSchedule
import kcrud.core.scheduler.service.SchedulerService
import kotlin.uuid.Uuid

/**
 * The scheduler dashboard route.
 */
@SchedulerRouteApi
internal fun Route.schedulerDashboardRoute() {
    get("/admin/scheduler/dashboard") {
        val groupId: Uuid? = call.queryParameters["group"]?.toUuidOrNull()
        val tasks: List<TaskSchedule> = SchedulerService.tasks.all(groupId = groupId)
        val content = ThymeleafContent(template = "scheduler/dashboard", model = mapOf("data" to tasks))
        call.respond(message = content)
    } api {
        tags = setOf("Scheduler Admin")
        summary = "Get the scheduler dashboard."
        description = "Get the scheduler dashboard, listing all scheduled tasks."
        operationId = "getSchedulerDashboard"
        queryParameter<Uuid>(name = "group") {
            description = "The group ID to filter tasks by."
            required = false
        }
        response<String>(status = HttpStatusCode.OK) {
            description = "The scheduler dashboard."
        }
    }
}
