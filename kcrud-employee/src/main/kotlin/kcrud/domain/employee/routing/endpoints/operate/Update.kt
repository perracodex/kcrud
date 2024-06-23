/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.domain.employee.routing.endpoints.operate

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kcrud.base.env.SessionContext
import kcrud.base.persistence.utils.toUUID
import kcrud.domain.employee.entity.EmployeeEntity
import kcrud.domain.employee.entity.EmployeeRequest
import kcrud.domain.employee.errors.EmployeeError
import kcrud.domain.employee.routing.annotation.EmployeeRouteAPI
import kcrud.domain.employee.service.EmployeeService
import org.koin.core.parameter.parametersOf
import org.koin.ktor.plugin.scope
import java.util.*

@EmployeeRouteAPI
internal fun Route.updateEmployeeById() {
    // Update an employee by ID.
    put<EmployeeRequest> { request ->
        val employeeId: UUID = call.parameters["employee_id"].toUUID()

        val sessionContext: SessionContext? = SessionContext.from(call = call)
        val service: EmployeeService = call.scope.get<EmployeeService> { parametersOf(sessionContext) }
        val updatedEmployee: EmployeeEntity? = service.update(
            employeeId = employeeId,
            employeeRequest = request
        )

        updatedEmployee?.let {
            call.respond(status = HttpStatusCode.OK, message = updatedEmployee)
        } ?: EmployeeError.EmployeeNotFound(employeeId = employeeId).raise()
    }
}
