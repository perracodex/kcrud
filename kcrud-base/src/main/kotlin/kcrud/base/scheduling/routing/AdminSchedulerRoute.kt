/*
 * Copyright (c) 2024-Present Perracodex. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.scheduling.routing

import io.ktor.server.routing.*
import kcrud.base.scheduling.routing.routes.deleteAllNotificationRoute
import kcrud.base.scheduling.routing.routes.deleteNotificationRoute
import kcrud.base.scheduling.routing.routes.listAllNotificationRoute

/**
 * Route administers all scheduled jobs, allowing to list and delete them.
 */
fun Route.adminSchedulerRoutes() {

    route("scheduler/jobs") {
        listAllNotificationRoute()
        deleteNotificationRoute()
        deleteAllNotificationRoute()
    }
}
