/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.server.demo.routing

import io.ktor.server.http.content.*
import io.ktor.server.routing.*
import kcrud.server.demo.DemoAPI
import kcrud.server.demo.routing.dashboard.dumpJsonRoute
import kcrud.server.demo.routing.dashboard.renderViewRoute
import kcrud.server.demo.routing.operate.createRecordsRoute
import kcrud.server.demo.routing.operate.deleteRecordsRoute

/**
 * Interactive employees demo endpoint.
 */
@OptIn(DemoAPI::class)
internal fun Route.demoRoutes() {

    // Configures the server to serve CSS files located in the 'demo' resources folder,
    // necessary for styling the Demo dashboard built with HTML DSL.
    staticResources(remotePath = "/static-demo", basePackage = "demo")

    route("demo") {
        createRecordsRoute()
        deleteRecordsRoute()
        renderViewRoute()
        dumpJsonRoute()
    }
}
