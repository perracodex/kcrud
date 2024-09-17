/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.scheduler.api.tasks.get

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kcrud.base.scheduler.service.core.SchedulerService

/**
 * Gets all scheduler task groups.
 */
internal fun Route.getSchedulerTaskGroupsRoute() {
    /**
     * Gets all scheduler task groups.
     * @OpenAPITag Scheduler
     */
    get("scheduler/task/group") {
        val groups: List<String> = SchedulerService.tasks.groups()
        call.respond(status = HttpStatusCode.OK, message = groups)
    }
}
