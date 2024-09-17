/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.domain.employee.api.operate

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kcrud.base.env.SessionContext
import kcrud.domain.employee.api.EmployeeRouteAPI
import kcrud.domain.employee.model.Employee
import kcrud.domain.employee.model.EmployeeRequest
import kcrud.domain.employee.service.EmployeeService
import org.koin.core.parameter.parametersOf
import org.koin.ktor.plugin.scope

@EmployeeRouteAPI
internal fun Route.createEmployeeRoute() {
    /**
     * Create a new employee.
     * @OpenAPITag Employee
     */
    post("v1/employees") {
        val sessionContext: SessionContext? = SessionContext.from(call = call)
        val request: EmployeeRequest = call.receive<EmployeeRequest>()
        val service: EmployeeService = call.scope.get<EmployeeService> { parametersOf(sessionContext) }
        val employee: Employee = service.create(request = request).getOrThrow()
        call.respond(status = HttpStatusCode.Created, message = employee)
    }
}
