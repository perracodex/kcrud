/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.server.demo.api.operate

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kcrud.core.context.getContext
import kcrud.domain.employee.service.EmployeeService
import kcrud.server.demo.DemoAPI
import org.koin.core.parameter.parametersOf
import org.koin.ktor.plugin.scope

@DemoAPI
internal fun Route.deleteRecordsRoute() {
    /**
     * Delete all demo records.
     * @OpenAPITag Demo
     */
    delete("demo") {
        val service: EmployeeService = call.scope.get<EmployeeService> { parametersOf(call.getContext()) }
        val count: Int = service.deleteAll()
        call.respond(status = HttpStatusCode.OK, message = "All employees deleted. Total: $count.")
    }
}
