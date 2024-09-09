/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.server.demo.routing.dashboard

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.routing.*
import kcrud.server.demo.DemoAPI
import kcrud.server.demo.DemoView

@DemoAPI
internal fun Route.renderViewRoute() {
    // Return demo records interactive HTML.
    get {
        call.respondHtml(status = HttpStatusCode.OK) {
            DemoView.build(html = this)
        }
    }
}
