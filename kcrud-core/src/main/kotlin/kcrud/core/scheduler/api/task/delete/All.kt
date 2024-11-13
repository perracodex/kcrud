/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.core.scheduler.api.task.delete

import io.github.perracodex.kopapi.dsl.operation.api
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kcrud.core.scheduler.api.SchedulerRouteApi
import kcrud.core.scheduler.service.SchedulerService

/**
 * Deletes all the scheduler tasks.
 */
@SchedulerRouteApi
internal fun Route.deleteAllSchedulerTasksRoute() {
    delete("/admin/scheduler/task") {
        val deletedCount: Int = SchedulerService.tasks.deleteAll()
        call.respond(status = HttpStatusCode.OK, message = deletedCount)
    } api {
        tags = setOf("Scheduler")
        summary = "Delete all scheduler tasks."
        description = "Delete all the scheduler tasks."
        operationId = "deleteAllSchedulerTasks"
        response<Int>(status = HttpStatusCode.OK) {
            description = "The number of tasks deleted."
        }
    }
}
