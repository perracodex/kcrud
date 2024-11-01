/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.core.scheduler.api.scheduler.operate

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kcrud.core.scheduler.api.SchedulerRouteApi
import kcrud.core.scheduler.model.task.TaskStateChange
import kcrud.core.scheduler.service.SchedulerService

/**
 * Resume all the scheduler tasks.
 */
@SchedulerRouteApi
internal fun Route.resumeSchedulerRoute() {
    /**
     * Resume all the scheduler tasks.
     * @OpenAPITag Scheduler - Maintenance
     */
    post("scheduler/resume") {
        val state: TaskStateChange = SchedulerService.resume()
        call.respond(status = HttpStatusCode.OK, message = state)
    }
}
