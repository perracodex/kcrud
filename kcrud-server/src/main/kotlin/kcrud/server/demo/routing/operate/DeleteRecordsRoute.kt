/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.server.demo.routing.operate

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kcrud.base.env.SessionContext
import kcrud.domain.employee.service.EmployeeService
import kcrud.server.demo.DemoAPI
import org.koin.core.parameter.parametersOf
import org.koin.ktor.plugin.scope

@DemoAPI
internal fun Route.deleteRecordsRoute() {
    // Delete all demo records.
    delete {
        val sessionContext: SessionContext? = SessionContext.from(call = call)
        val service: EmployeeService = call.scope.get<EmployeeService> { parametersOf(sessionContext) }
        val count: Int = service.deleteAll()
        call.respond(status = HttpStatusCode.OK, message = "All employees deleted. Total: $count.")
    }
}
