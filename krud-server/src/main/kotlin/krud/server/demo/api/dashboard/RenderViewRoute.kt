/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package krud.server.demo.api.dashboard

import io.github.perracodex.kopapi.dsl.operation.api
import io.ktor.http.*
import io.ktor.server.html.*
import io.ktor.server.routing.*
import krud.server.demo.DemoApi
import krud.server.demo.DemoView

@DemoApi
internal fun Route.renderViewRoute() {
    get("/demo") {
        call.respondHtml(status = HttpStatusCode.OK) {
            DemoView.build(html = this)
        }
    } api {
        tags = setOf("Demo")
        summary = "Render the demo view."
        description = "Render the demo view."
        operationId = "renderView"
        response<String>(status = HttpStatusCode.OK) {
            description = "Demo view."
        }
        basicSecurity(name = "Demo Authentication") {
            description = "Access to demo data."
        }
    }
}
