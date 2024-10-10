/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.core.scheduler.api.view

import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.thymeleaf.*
import kcrud.core.persistence.utils.toUuidOrNull
import kcrud.core.scheduler.api.SchedulerRouteAPI
import kcrud.core.scheduler.model.task.TaskSchedule
import kcrud.core.scheduler.service.SchedulerService
import kotlin.uuid.Uuid

/**
 * The scheduler dashboard route.
 */
@SchedulerRouteAPI
internal fun Route.schedulerDashboardRoute() {
    /**
     * The scheduler dashboard route.
     * @OpenAPITag Scheduler - Maintenance
     */
    get("scheduler/dashboard") {
        val groupId: Uuid? = call.parameters["group"]?.toUuidOrNull()
        val tasks: List<TaskSchedule> = SchedulerService.tasks.all(groupId = groupId)
        val content = ThymeleafContent(template = "scheduler/dashboard", model = mapOf("data" to tasks))
        call.respond(message = content)
    }
}
