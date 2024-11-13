/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.core.scheduler.api.task.operate

import io.github.perracodex.kopapi.dsl.operation.api
import io.github.perracodex.kopapi.dsl.parameter.pathParameter
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import kcrud.core.scheduler.api.SchedulerRouteApi
import kcrud.core.scheduler.service.SchedulerService

/**
 * Resends a concrete scheduler task.
 */
@SchedulerRouteApi
internal fun Route.resendSchedulerTaskRoute() {
    post("/admin/scheduler/task/{name}/{group}/resend") {
        val name: String = call.parameters.getOrFail(name = "name")
        val group: String = call.parameters.getOrFail(name = "group")
        SchedulerService.tasks.resend(name = name, group = group)
        call.respond(HttpStatusCode.OK)
    } api {
        tags = setOf("Scheduler")
        summary = "Resend a scheduler task."
        description = "Resend a concrete scheduler task."
        operationId = "resendSchedulerTask"
        pathParameter<String>(name = "name") {
            description = "The name of the task."
        }
        pathParameter<String>(name = "group") {
            description = "The group of the task."
        }
        response<Unit>(status = HttpStatusCode.OK) {
            description = "The task has been resent."
        }
    }
}
