/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package krud.domain.employee.api.operate

import io.github.perracodex.kopapi.dsl.operation.api
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import krud.base.context.getContext
import krud.domain.employee.api.EmployeeRouteApi
import krud.domain.employee.model.Employee
import krud.domain.employee.model.EmployeeRequest
import krud.domain.employee.service.EmployeeService
import org.koin.core.parameter.parametersOf
import org.koin.ktor.plugin.scope

@EmployeeRouteApi
internal fun Route.createEmployeeRoute() {
    post("/api/v1/employees") {
        val request: EmployeeRequest = call.receive<EmployeeRequest>()
        val service: EmployeeService = call.scope.get<EmployeeService> { parametersOf(call.getContext()) }
        val employee: Employee = service.create(request = request).getOrThrow()
        call.respond(status = HttpStatusCode.Created, message = employee)
    } api {
        tags = setOf("Employee")
        summary = "Create an employee."
        description = "Create a new employee in the system."
        operationId = "createEmployee"
        requestBody<EmployeeRequest> {
            description = "The employee to create."
        }
        response<Employee>(status = HttpStatusCode.Created) {
            description = "Employee created."
        }
        bearerSecurity(name = "Authentication") {
            description = "Access to employee data."
        }
    }
}
