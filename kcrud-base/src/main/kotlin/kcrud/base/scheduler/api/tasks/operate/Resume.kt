/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.scheduler.api.tasks.operate

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import kcrud.base.scheduler.api.SchedulerRouteAPI
import kcrud.base.scheduler.model.task.TaskStateChange
import kcrud.base.scheduler.service.core.SchedulerService

/**
 * Resume a concrete scheduler task.
 */
@SchedulerRouteAPI
internal fun Route.resumeSchedulerTaskRoute() {
    /**
     * Resume a concrete scheduled task.
     * @OpenAPITag Scheduler
     */
    post("scheduler/task/{name}/{group}/resume") {
        val name: String = call.parameters.getOrFail(name = "name")
        val group: String = call.parameters.getOrFail(name = "group")
        val state: TaskStateChange = SchedulerService.tasks.resume(name = name, group = group)
        call.respond(status = HttpStatusCode.OK, message = state)
    }
}
