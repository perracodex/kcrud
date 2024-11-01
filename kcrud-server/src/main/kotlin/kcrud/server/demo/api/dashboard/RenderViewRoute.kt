/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.server.demo.api.dashboard

import io.ktor.http.*
import io.ktor.server.html.*
import io.ktor.server.routing.*
import kcrud.server.demo.DemoApi
import kcrud.server.demo.DemoView

@DemoApi
internal fun Route.renderViewRoute() {
    /**
     * Display the demo view.
     * @OpenAPITag Demo
     */
    get("demo") {
        call.respondHtml(status = HttpStatusCode.OK) {
            DemoView.build(html = this)
        }
    }
}
