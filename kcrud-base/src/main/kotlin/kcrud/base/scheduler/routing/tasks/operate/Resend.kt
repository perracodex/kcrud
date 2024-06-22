/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.scheduler.routing.tasks.operate

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kcrud.base.scheduler.service.core.SchedulerService

/**
 * Resends a concrete scheduler task.
 */
fun Route.resendSchedulerTaskRoute() {
    // Resends a concrete scheduler task.
    post("{name}/{group}/resend") {
        val name: String = call.parameters["name"]!!
        val group: String = call.parameters["group"]!!
        SchedulerService.tasks.resend(name = name, group = group)
        call.respond(HttpStatusCode.OK)
    }
}
