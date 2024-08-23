/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.scheduler.routing.view

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.thymeleaf.*
import kcrud.base.persistence.utils.toUUIDOrNull
import kcrud.base.scheduler.entity.TaskScheduleEntity
import kcrud.base.scheduler.service.core.SchedulerService
import java.util.*

/**
 * The scheduler dashboard route.
 */
/**
 * The scheduler dashboard route.
 */
fun Route.schedulerDashboardRoute() {

    // The scheduler dashboard route.
    get("dashboard") {
        val groupId: UUID? = call.parameters["group"]?.toUUIDOrNull()
        val tasks: List<TaskScheduleEntity> = SchedulerService.tasks.all(groupId = groupId)
        val content = ThymeleafContent(template = "scheduler/dashboard", model = mapOf("data" to tasks))
        call.respond(message = content)
    }
}
