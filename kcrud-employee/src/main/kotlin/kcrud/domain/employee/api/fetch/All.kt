/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.domain.employee.api.fetch

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.perracodex.exposed.pagination.Page
import io.perracodex.exposed.pagination.getPageable
import kcrud.base.env.CallContext.Companion.getContext
import kcrud.domain.employee.api.EmployeeRouteAPI
import kcrud.domain.employee.model.Employee
import kcrud.domain.employee.service.EmployeeService
import org.koin.core.parameter.parametersOf
import org.koin.ktor.plugin.scope

@EmployeeRouteAPI
internal fun Route.findAllEmployeesRoute() {
    /**
     * Find all employees.
     * @OpenAPITag Employee
     */
    get("v1/employees") {
        val service: EmployeeService = call.scope.get<EmployeeService> { parametersOf(call.getContext()) }
        val employees: Page<Employee> = service.findAll(pageable = call.getPageable())
        call.respond(status = HttpStatusCode.OK, message = employees)
    }
}
