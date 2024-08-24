/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.server.demo

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.html.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kcrud.base.env.SessionContext
import kcrud.base.persistence.pagination.Page
import kcrud.base.persistence.pagination.Pageable
import kcrud.base.persistence.pagination.getPageable
import kcrud.domain.employee.service.EmployeeService
import kcrud.domain.employment.entity.EmploymentEntity
import kcrud.domain.employment.service.EmploymentService
import org.koin.core.parameter.parametersOf
import org.koin.ktor.plugin.scope

/**
 * Interactive employees demo endpoint.
 */
internal fun Route.employeesDemoRoute() {

    // Required so the HTML for can find its respective CSS file.
    staticResources(remotePath = "/static-demo", basePackage = "demo")

    route("demo") {

        getDemoRecords()

        createDemoRecords()

        deleteDemoRecords()

        dumpJson()
    }
}

@OptIn(DemoAPI::class)
private fun Route.getDemoRecords() {
    // Return demo records interactive HTML.
    get {
        call.respondHtml(status = HttpStatusCode.OK) {
            DemoView.build(html = this)
        }
    }
}

@OptIn(DemoAPI::class)
private fun Route.createDemoRecords() {
    val maxAllowedBatch = 100_000

    // Create a batch of demo records.
    post {
        val count: Int? = call.request.queryParameters["count"]?.toIntOrNull()

        if (count != null && count in 1..maxAllowedBatch) {
            DemoUtils.createDemoRecords(call = call, count = count)
            call.respond(status = HttpStatusCode.OK, "Created $count employees.")
        } else {
            call.respond(status = HttpStatusCode.BadRequest, message = "Invalid count. Must be between 1 and $maxAllowedBatch.")
        }
    }
}

private fun Route.deleteDemoRecords() {
    // Delete all demo records.
    delete {
        val sessionContext: SessionContext? = SessionContext.from(call = call)
        val service: EmployeeService = call.scope.get<EmployeeService> { parametersOf(sessionContext) }
        val count: Int = service.deleteAll()
        call.respond(status = HttpStatusCode.OK, message = "All employees deleted. Total: $count.")
    }
}

private fun Route.dumpJson() {
    // Return all demo records as JSON.
    get("json") {
        val page: Page<EmploymentEntity> = call.getEmployments()
        call.respond(status = HttpStatusCode.OK, message = page)
    }
}

private suspend fun ApplicationCall.getEmployments(): Page<EmploymentEntity> {
    val sessionContext: SessionContext? = this.principal<SessionContext>()
    val service: EmploymentService = this.scope.get<EmploymentService> { parametersOf(sessionContext) }
    val pageable: Pageable? = this.getPageable()
    return service.findAll(pageable = pageable)
}
