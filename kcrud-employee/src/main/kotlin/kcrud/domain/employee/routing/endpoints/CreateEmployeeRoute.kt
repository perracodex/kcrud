/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.domain.employee.routing.endpoints

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kcrud.base.env.SessionContext
import kcrud.domain.employee.entity.EmployeeEntity
import kcrud.domain.employee.entity.EmployeeRequest
import kcrud.domain.employee.routing.annotation.EmployeeRouteAPI
import kcrud.domain.employee.service.EmployeeService
import org.koin.core.parameter.parametersOf
import org.koin.ktor.plugin.scope

@EmployeeRouteAPI
internal fun Route.createEmployee() {
    // Create a new employee.
    post<EmployeeRequest> { employeeRequest ->
        val sessionContext: SessionContext? = call.principal<SessionContext>()
        val service: EmployeeService = call.scope.get<EmployeeService> { parametersOf(sessionContext) }
        val createdEmployee: EmployeeEntity = service.create(employeeRequest = employeeRequest)
        call.respond(status = HttpStatusCode.Created, message = createdEmployee)
    }
}
