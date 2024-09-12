/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.domain.employee.routing.endpoints.get

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kcrud.base.env.SessionContext
import kcrud.base.persistence.pagination.Page
import kcrud.base.persistence.pagination.getPageable
import kcrud.domain.employee.model.EmployeeDto
import kcrud.domain.employee.model.EmployeeFilterSet
import kcrud.domain.employee.routing.annotation.EmployeeRouteAPI
import kcrud.domain.employee.service.EmployeeService
import org.koin.core.parameter.parametersOf
import org.koin.ktor.plugin.scope

@EmployeeRouteAPI
internal fun Route.searchEmployeeRoute() {
    // Search (Filter) employees.
    post<EmployeeFilterSet>("/search") { request ->
        val sessionContext: SessionContext? = SessionContext.from(call = call)
        val service: EmployeeService = call.scope.get<EmployeeService> { parametersOf(sessionContext) }
        val employees: Page<EmployeeDto> = service.search(filterSet = request, pageable = call.getPageable())
        call.respond(status = HttpStatusCode.OK, message = employees)
    }
}
