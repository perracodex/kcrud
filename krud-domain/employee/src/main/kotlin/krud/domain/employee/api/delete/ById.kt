/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package krud.domain.employee.api.delete

import io.github.perracodex.kopapi.dsl.operation.api
import io.github.perracodex.kopapi.dsl.parameter.pathParameter
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import krud.base.context.getContext
import krud.base.util.toUuid
import krud.domain.employee.api.EmployeeRouteApi
import krud.domain.employee.service.EmployeeService
import org.koin.core.parameter.parametersOf
import org.koin.ktor.plugin.scope
import kotlin.uuid.Uuid

@EmployeeRouteApi
internal fun Route.deleteEmployeeByIdRoute() {
    delete("/api/v1/employees/{employee_id}") {
        val employeeId: Uuid = call.parameters.getOrFail(name = "employee_id").toUuid()
        val service: EmployeeService = call.scope.get<EmployeeService> { parametersOf(call.getContext()) }
        val deletedCount: Int = service.delete(employeeId = employeeId)
        call.respond(status = HttpStatusCode.OK, message = deletedCount)
    } api {
        tags = setOf("Employee")
        summary = "Delete an employee by ID."
        description = "Delete an employee by their unique ID."
        operationId = "deleteEmployeeById"
        pathParameter<Uuid>(name = "employee_id") {
            description = "The unique identifier of the employee."
        }
        response<Int>(status = HttpStatusCode.OK) {
            description = "Number of employees deleted."
        }
        bearerSecurity(name = "Authentication") {
            description = "Access to employee data."
        }
    }
}
