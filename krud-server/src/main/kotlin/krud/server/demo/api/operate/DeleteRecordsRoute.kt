/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package krud.server.demo.api.operate

import io.github.perracodex.kopapi.dsl.operation.api
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import krud.base.context.getContext
import krud.domain.employee.service.EmployeeService
import krud.server.demo.DemoApi
import org.koin.core.parameter.parametersOf
import org.koin.ktor.plugin.scope

@DemoApi
internal fun Route.deleteRecordsRoute() {
    delete("/demo") {
        val service: EmployeeService = call.scope.get<EmployeeService> { parametersOf(call.getContext()) }
        val count: Int = service.deleteAll()
        call.respond(status = HttpStatusCode.OK, message = "All employees deleted. Total: $count.")
    } api {
        tags = setOf("Demo")
        summary = "Delete all demo records."
        description = "Delete all demo records in the system."
        operationId = "deleteRecords"
        response<String>(status = HttpStatusCode.OK) {
            description = "Message indicating the number of employees deleted."
        }
        basicSecurity(name = "Demo Authentication") {
            description = "Access to demo data."
        }
    }
}
