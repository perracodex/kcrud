/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.scheduler.routing.tasks.operate

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import kcrud.base.scheduler.model.task.TaskStateChange
import kcrud.base.scheduler.service.core.SchedulerService

/**
 * Pause a concrete scheduler task.
 */
internal fun Route.pauseSchedulerTaskRoute() {
    // Pause a concrete scheduler task.
    post("scheduler/task/{name}/{group}/pause") {
        val name: String = call.parameters.getOrFail(name = "name")
        val group: String = call.parameters.getOrFail(name = "group")
        val state: TaskStateChange = SchedulerService.tasks.pause(name = name, group = group)
        call.respond(status = HttpStatusCode.OK, message = state)
    }
}
