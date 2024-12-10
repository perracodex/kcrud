/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package krud.domain.employee.api.fetch

import io.github.perracodex.kopapi.dsl.operation.api
import io.github.perracodex.kopapi.dsl.parameter.queryParameter
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import io.perracodex.exposed.pagination.Page
import io.perracodex.exposed.pagination.getPageable
import krud.core.context.getContext
import krud.domain.employee.api.EmployeeRouteApi
import krud.domain.employee.model.Employee
import krud.domain.employee.service.EmployeeService
import org.koin.core.parameter.parametersOf
import org.koin.ktor.plugin.scope

@EmployeeRouteApi
internal fun Route.searchEmployeeRoute() {
    get("/api/v1/employees/search") {
        val term: String = call.request.queryParameters.getOrFail(name = "term")
        val service: EmployeeService = call.scope.get<EmployeeService> { parametersOf(call.getContext()) }
        val employees: Page<Employee> = service.search(term = term, pageable = call.getPageable())
        call.respond(status = HttpStatusCode.OK, message = employees)
    } api {
        tags = setOf("Employee")
        summary = "Search for employees."
        description = "Search for employees based on the given search term."
        operationId = "searchEmployees"
        queryParameter<String>(name = "term") {
            description = "The search term to use."
        }
        response<Page<Employee>>(status = HttpStatusCode.OK) {
            description = "Employees found."
        }
        bearerSecurity(name = "Authentication") {
            description = "Access to employee data."
        }
    }
}
