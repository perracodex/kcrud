/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.scheduler.routing.get

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kcrud.base.scheduler.entity.TaskScheduleEntity
import kcrud.base.scheduler.service.SchedulerService

/**
 * Gets all scheduler tasks.
 */
fun Route.getSchedulerTasksRoute() {
    // Gets all scheduler tasks.
    get {
        val tasks: List<TaskScheduleEntity> = SchedulerService.getTasks()
        call.respond(status = HttpStatusCode.OK, message = tasks)
    }
}
