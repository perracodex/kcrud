/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.domain.employee.api.operate

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import kcrud.core.env.SessionContext.Companion.getContext
import kcrud.core.persistence.utils.toUuid
import kcrud.domain.employee.api.EmployeeRouteAPI
import kcrud.domain.employee.errors.EmployeeError
import kcrud.domain.employee.model.Employee
import kcrud.domain.employee.model.EmployeeRequest
import kcrud.domain.employee.service.EmployeeService
import org.koin.core.parameter.parametersOf
import org.koin.ktor.plugin.scope
import kotlin.uuid.Uuid

@EmployeeRouteAPI
internal fun Route.updateEmployeeByIdRoute() {
    /**
     * Update an employee by ID.
     * @OpenAPITag Employee
     */
    put("v1/employees/{employee_id}") {
        val employeeId: Uuid = call.parameters.getOrFail(name = "employee_id").toUuid()
        val request: EmployeeRequest = call.receive<EmployeeRequest>()

        val service: EmployeeService = call.scope.get<EmployeeService> { parametersOf(call.getContext()) }
        val updatedEmployee: Employee? = service.update(
            employeeId = employeeId,
            request = request
        ).getOrThrow()

        if (updatedEmployee == null) {
            throw EmployeeError.EmployeeNotFound(employeeId = employeeId)
        } else {
            call.respond(status = HttpStatusCode.OK, message = updatedEmployee)
        }
    }
}
