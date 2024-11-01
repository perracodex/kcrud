/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.core.scheduler.api.tasks.fetch

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kcrud.core.scheduler.api.SchedulerRouteApi
import kcrud.core.scheduler.service.SchedulerService

/**
 * Gets all scheduler task groups.
 */
@SchedulerRouteApi
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
