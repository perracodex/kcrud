/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.domain.employee.api.delete

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kcrud.core.context.getContext
import kcrud.domain.employee.api.EmployeeRouteApi
import kcrud.domain.employee.service.EmployeeService
import org.koin.core.parameter.parametersOf
import org.koin.ktor.plugin.scope

@EmployeeRouteApi
internal fun Route.deleteAllEmployeesRoute() {
    /**
     * Delete all employees.
     * @OpenAPITag Employee
     */
    delete("v1/employees") {
        val service: EmployeeService = call.scope.get<EmployeeService> { parametersOf(call.getContext()) }
        val deletedCount: Int = service.deleteAll()
        call.respond(status = HttpStatusCode.OK, message = deletedCount)
    }
}
