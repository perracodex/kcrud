/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.server.demo.api

import io.ktor.server.http.content.*
import io.ktor.server.routing.*
import kcrud.server.demo.DemoApi
import kcrud.server.demo.api.dashboard.dumpJsonRoute
import kcrud.server.demo.api.dashboard.renderViewRoute
import kcrud.server.demo.api.operate.createRecordsRoute
import kcrud.server.demo.api.operate.deleteRecordsRoute

/**
 * Interactive employees demo endpoint.
 */
@OptIn(DemoApi::class)
internal fun Route.demoRoutes() {
    // Configures the server to serve CSS files located in the 'demo' resources folder,
    // necessary for styling the Demo dashboard built with HTML DSL.
    staticResources(remotePath = "/static-demo", basePackage = "demo")

    createRecordsRoute()
    deleteRecordsRoute()
    renderViewRoute()
    dumpJsonRoute()
}
