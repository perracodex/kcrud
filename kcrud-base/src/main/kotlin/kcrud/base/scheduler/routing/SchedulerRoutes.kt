/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.scheduler.routing

import io.ktor.server.routing.*
import kcrud.base.scheduler.routing.scheduler.pauseSchedulerRoute
import kcrud.base.scheduler.routing.scheduler.resumeSchedulerRoute
import kcrud.base.scheduler.routing.scheduler.schedulerStateRoute
import kcrud.base.scheduler.routing.tasks.delete.deleteAllSchedulerTasksRoute
import kcrud.base.scheduler.routing.tasks.delete.deleteSchedulerTaskRoute
import kcrud.base.scheduler.routing.tasks.get.getSchedulerTaskGroupsRoute
import kcrud.base.scheduler.routing.tasks.get.getSchedulerTasksRoute
import kcrud.base.scheduler.routing.tasks.state.pauseSchedulerTaskRoute
import kcrud.base.scheduler.routing.tasks.state.resumeSchedulerTaskRoute
import kcrud.base.scheduler.routing.view.schedulerDashboardRoute

/**
 * Route administers all scheduled tasks, allowing to list and delete them.
 */
fun Route.schedulerRoutes() {

    route("scheduler") {
        schedulerDashboardRoute()
        schedulerStateRoute()
        pauseSchedulerRoute()
        resumeSchedulerRoute()

        route("task") {
            getSchedulerTasksRoute()
            getSchedulerTaskGroupsRoute()

            deleteSchedulerTaskRoute()
            deleteAllSchedulerTasksRoute()

            pauseSchedulerTaskRoute()
            resumeSchedulerTaskRoute()
        }
    }
}
