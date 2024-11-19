/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.scheduler.api.view

import io.ktor.server.http.content.*
import io.ktor.server.routing.*

/**
 * The scheduler dashboard route.
 */
internal fun Route.schedulerDashboardRoute() {
    // Register the static resources for the scheduler dashboard, (js, css, etc).
    staticResources(remotePath = "/scheduler", basePackage = "/scheduler")

    // Serve the dashboard page.
    staticResources(remotePath = "/admin/scheduler/dashboard", basePackage = "/scheduler") {
        default("dashboard.html")
    }
}
