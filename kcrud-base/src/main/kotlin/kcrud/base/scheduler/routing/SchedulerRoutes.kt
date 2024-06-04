/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.scheduler.routing

import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.routing.*
import kcrud.base.plugins.RateLimitScope
import kcrud.base.scheduler.routing.delete.deleteAllSchedulerTasksRoute
import kcrud.base.scheduler.routing.delete.deleteSchedulerTaskRoute
import kcrud.base.scheduler.routing.get.getSchedulerTaskGroupsRoute
import kcrud.base.scheduler.routing.get.getSchedulerTasksRoute
import kcrud.base.scheduler.routing.state.pauseAllSchedulerTasksRoute
import kcrud.base.scheduler.routing.state.pauseSchedulerTaskRoute
import kcrud.base.scheduler.routing.state.resumeAllSchedulerTasksRoute
import kcrud.base.scheduler.routing.state.resumeSchedulerTaskRoute
import kcrud.base.scheduler.routing.view.schedulerDashboardRoute

/**
 * Route administers all scheduled tasks, allowing to list and delete them.
 */
fun Route.schedulerRoutes() {

    rateLimit(configuration = RateLimitName(name = RateLimitScope.PRIVATE_API.key)) {
        route("scheduler/tasks") {
            schedulerDashboardRoute()

            getSchedulerTasksRoute()
            getSchedulerTaskGroupsRoute()

            deleteSchedulerTaskRoute()
            deleteAllSchedulerTasksRoute()

            pauseAllSchedulerTasksRoute()
            pauseSchedulerTaskRoute()

            resumeAllSchedulerTasksRoute()
            resumeSchedulerTaskRoute()
        }
    }
}
