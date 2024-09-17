/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.scheduler.api.tasks.delete

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import kcrud.base.scheduler.api.SchedulerRouteAPI
import kcrud.base.scheduler.service.core.SchedulerService

/**
 * Deletes a concrete scheduler task.
 */
@SchedulerRouteAPI
internal fun Route.deleteSchedulerTaskRoute() {
    /**
     * Deletes a concrete scheduler task.
     * @OpenAPITag Scheduler
     */
    delete("scheduler/task/{name}/{group}") {
        val name: String = call.parameters.getOrFail(name = "name")
        val group: String = call.parameters.getOrFail(name = "group")
        val deletedCount: Int = SchedulerService.tasks.delete(name = name, group = group)
        call.respond(status = HttpStatusCode.OK, message = deletedCount)
    }
}
