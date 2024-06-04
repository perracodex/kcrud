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
import kcrud.base.persistence.pagination.Page
import kcrud.domain.employee.entity.EmployeeEntity
import kcrud.domain.employee.entity.EmployeeFilterSet
import kcrud.domain.employee.routing.annotation.EmployeeRouteAPI
import kcrud.domain.employee.service.EmployeeService
import org.koin.core.parameter.parametersOf
import org.koin.ktor.plugin.scope

@EmployeeRouteAPI
internal fun Route.filterEmployees() {
    // Search (Filter) employees.
    post<EmployeeFilterSet>("/search") { employeeFilterSet ->
        val sessionContext: SessionContext? = call.principal<SessionContext>()
        val service: EmployeeService = call.scope.get<EmployeeService> { parametersOf(sessionContext) }
        val employees: Page<EmployeeEntity> = service.filter(filterSet = employeeFilterSet)
        call.respond(status = HttpStatusCode.OK, message = employees)
    }
}
