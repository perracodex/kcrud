/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.scheduler.routing

import io.ktor.server.http.content.*
import io.ktor.server.routing.*
import kcrud.base.scheduler.routing.scheduler.audit.schedulerAllAuditRoute
import kcrud.base.scheduler.routing.scheduler.audit.schedulerAuditByTaskRoute
import kcrud.base.scheduler.routing.scheduler.pauseSchedulerRoute
import kcrud.base.scheduler.routing.scheduler.restartSchedulerRoute
import kcrud.base.scheduler.routing.scheduler.resumeSchedulerRoute
import kcrud.base.scheduler.routing.scheduler.schedulerStateRoute
import kcrud.base.scheduler.routing.tasks.delete.deleteAllSchedulerTasksRoute
import kcrud.base.scheduler.routing.tasks.delete.deleteSchedulerTaskRoute
import kcrud.base.scheduler.routing.tasks.get.getSchedulerTaskGroupsRoute
import kcrud.base.scheduler.routing.tasks.get.getSchedulerTasksRoute
import kcrud.base.scheduler.routing.tasks.operate.pauseSchedulerTaskRoute
import kcrud.base.scheduler.routing.tasks.operate.resendSchedulerTaskRoute
import kcrud.base.scheduler.routing.tasks.operate.resumeSchedulerTaskRoute
import kcrud.base.scheduler.routing.view.schedulerDashboardRoute

/**
 * Route administers all scheduled tasks, allowing to list and delete them.
 */
public fun Route.schedulerRoutes() {

    // Sets up the routing to serve resources as static content for the scheduler.
    staticResources(remotePath = "/templates/scheduler", basePackage = "/templates/scheduler")

    // Maintenance related routes.
    schedulerDashboardRoute()
    schedulerStateRoute()
    pauseSchedulerRoute()
    resumeSchedulerRoute()
    restartSchedulerRoute()
    schedulerAllAuditRoute()
    schedulerAuditByTaskRoute()

    // Task related routes.
    getSchedulerTasksRoute()
    getSchedulerTaskGroupsRoute()
    deleteSchedulerTaskRoute()
    deleteAllSchedulerTasksRoute()
    pauseSchedulerTaskRoute()
    resumeSchedulerTaskRoute()
    resendSchedulerTaskRoute()
}
