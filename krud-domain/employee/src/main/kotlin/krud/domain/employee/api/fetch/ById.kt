/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package krud.domain.employee.api.fetch

import io.github.perracodex.kopapi.dsl.operation.api
import io.github.perracodex.kopapi.dsl.parameter.pathParameter
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import krud.core.context.getContext
import krud.core.error.AppException
import krud.core.util.toUuid
import krud.domain.employee.api.EmployeeRouteApi
import krud.domain.employee.error.EmployeeError
import krud.domain.employee.model.Employee
import krud.domain.employee.service.EmployeeService
import org.koin.core.parameter.parametersOf
import org.koin.ktor.plugin.scope
import kotlin.uuid.Uuid

@EmployeeRouteApi
internal fun Route.findEmployeeByIdRoute() {
    get("/api/v1/employees/{employee_id}") {
        val employeeId: Uuid = call.parameters.getOrFail(name = "employee_id").toUuid()
        val service: EmployeeService = call.scope.get<EmployeeService> { parametersOf(call.getContext()) }
        val employee: Employee = service.findById(employeeId = employeeId)
            ?: throw EmployeeError.EmployeeNotFound(employeeId = employeeId)
        call.respond(status = HttpStatusCode.OK, message = employee)
    } api {
        tags = setOf("Employee")
        summary = "Find an employee by ID."
        description = "Retrieve an employee's details by their unique ID"
        operationId = "findEmployeeById"
        pathParameter<Uuid>(name = "employee_id") {
            description = "The unique identifier of the employee."
        }
        response<Employee>(status = HttpStatusCode.OK) {
            description = "Employee found."
        }
        response<AppException.ErrorResponse>(status = EmployeeError.EmployeeNotFound.STATUS_CODE) {
            description = "Employee not found. Code: ${EmployeeError.EmployeeNotFound.ERROR_CODE}"
        }
        bearerSecurity(name = "Authentication") {
            description = "Access to employee data."
        }
    }
}
