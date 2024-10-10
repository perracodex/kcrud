/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.domain.employee.api.fetch

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import io.perracodex.exposed.pagination.Page
import io.perracodex.exposed.pagination.getPageable
import kcrud.core.context.getContext
import kcrud.domain.employee.api.EmployeeRouteAPI
import kcrud.domain.employee.model.Employee
import kcrud.domain.employee.service.EmployeeService
import org.koin.core.parameter.parametersOf
import org.koin.ktor.plugin.scope

@EmployeeRouteAPI
internal fun Route.searchEmployeeRoute() {
    /**
     * Search for employees given a search term. Can be partial.
     * @OpenAPITag Employee
     */
    get("v1/employees/search/{term?}") {
        val term: String = call.request.queryParameters.getOrFail(name = "term")
        val service: EmployeeService = call.scope.get<EmployeeService> { parametersOf(call.getContext()) }
        val employees: Page<Employee> = service.search(term = term, pageable = call.getPageable())
        call.respond(status = HttpStatusCode.OK, message = employees)
    }
}
