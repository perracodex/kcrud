/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package krud.server.demo.api.dashboard

import io.github.perracodex.kopapi.dsl.operation.api
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.perracodex.exposed.pagination.Page
import io.perracodex.exposed.pagination.getPageable
import krud.base.context.getContext
import krud.domain.employee.model.Employee
import krud.domain.employee.service.EmployeeService
import krud.server.demo.DemoApi
import org.koin.core.parameter.parametersOf
import org.koin.ktor.plugin.scope

@DemoApi
internal fun Route.dumpJsonRoute() {
    get("/demo/json") {
        val service: EmployeeService = call.scope.get<EmployeeService> { parametersOf(call.getContext()) }
        val page: Page<Employee> = service.findAll(pageable = call.getPageable())
        call.respond(status = HttpStatusCode.OK, message = page)
    } api {
        tags = setOf("Demo")
        summary = "Dump all demo records as JSON."
        description = "Dump all demo records as JSON."
        operationId = "dumpJson"
        response<Page<Employee>>(status = HttpStatusCode.OK) {
            description = "Page of demo records."
        }
        basicSecurity(name = "Demo Authentication") {
            description = "Access to demo data."
        }
    }
}
