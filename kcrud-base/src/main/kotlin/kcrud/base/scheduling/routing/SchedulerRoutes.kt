/*
 * Copyright (c) 2024-Present Perracodex. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.scheduling.routing

import io.ktor.server.routing.*
import kcrud.base.scheduling.routing.delete.deleteAllScheduledJobsRoute
import kcrud.base.scheduling.routing.delete.deleteScheduledJobRoute
import kcrud.base.scheduling.routing.get.getScheduledJobGroupRoute
import kcrud.base.scheduling.routing.get.getScheduledJobsRoute
import kcrud.base.scheduling.routing.state.pauseScheduledJobsRoute
import kcrud.base.scheduling.routing.state.resumeScheduledJobsRoute
import kcrud.base.scheduling.routing.view.schedulerDashboardRoute

/**
 * Route administers all scheduled jobs, allowing to list and delete them.
 */
fun Route.schedulerRoutes() {

    route("scheduler/jobs") {
        schedulerDashboardRoute()

        getScheduledJobsRoute()
        getScheduledJobGroupRoute()

        deleteScheduledJobRoute()
        deleteAllScheduledJobsRoute()

        pauseScheduledJobsRoute()
        resumeScheduledJobsRoute()
    }
}
