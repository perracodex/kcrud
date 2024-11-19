/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.scheduler.api

import io.ktor.server.routing.*
import kcrud.scheduler.api.scheduler.audit.schedulerAllAuditRoute
import kcrud.scheduler.api.scheduler.audit.schedulerAuditByTaskRoute
import kcrud.scheduler.api.scheduler.operate.pauseSchedulerRoute
import kcrud.scheduler.api.scheduler.operate.restartSchedulerRoute
import kcrud.scheduler.api.scheduler.operate.resumeSchedulerRoute
import kcrud.scheduler.api.scheduler.operate.schedulerStateRoute
import kcrud.scheduler.api.task.delete.deleteAllSchedulerTasksRoute
import kcrud.scheduler.api.task.delete.deleteSchedulerGroupRoute
import kcrud.scheduler.api.task.delete.deleteSchedulerTaskRoute
import kcrud.scheduler.api.task.fetch.getSchedulerAllGroupsRoute
import kcrud.scheduler.api.task.fetch.getSchedulerTasksRoute
import kcrud.scheduler.api.task.operate.pauseSchedulerTaskRoute
import kcrud.scheduler.api.task.operate.resendSchedulerTaskRoute
import kcrud.scheduler.api.task.operate.resumeSchedulerTaskRoute
import kcrud.scheduler.api.view.schedulerDashboardRoute

/**
 * Route administers all scheduled tasks, allowing to list and delete them.
 */
public fun Route.schedulerRoutes() {

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
    getSchedulerAllGroupsRoute()
    deleteSchedulerTaskRoute()
    deleteSchedulerGroupRoute()
    deleteAllSchedulerTasksRoute()
    pauseSchedulerTaskRoute()
    resumeSchedulerTaskRoute()
    resendSchedulerTaskRoute()
}
