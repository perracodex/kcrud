/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package krud.domain.employee.api.delete

import io.github.perracodex.kopapi.dsl.operation.api
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import krud.core.context.getContext
import krud.domain.employee.api.EmployeeRouteApi
import krud.domain.employee.service.EmployeeService
import org.koin.core.parameter.parametersOf
import org.koin.ktor.plugin.scope

@EmployeeRouteApi
internal fun Route.deleteAllEmployeesRoute() {
    delete("/api/v1/employees") {
        val service: EmployeeService = call.scope.get<EmployeeService> { parametersOf(call.getContext()) }
        val deletedCount: Int = service.deleteAll()
        call.respond(status = HttpStatusCode.OK, message = deletedCount)
    } api {
        tags = setOf("Employee")
        summary = "Delete all employees."
        description = "Delete all employees in the system."
        operationId = "deleteAllEmployees"
        response<Int>(status = HttpStatusCode.OK) {
            description = "Number of employees deleted."
        }
        bearerSecurity(name = "Authentication") {
            description = "Access to employee data."
        }
    }
}
