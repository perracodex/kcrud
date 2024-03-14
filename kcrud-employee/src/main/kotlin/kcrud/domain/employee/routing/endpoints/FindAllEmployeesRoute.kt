/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.domain.employee.routing.endpoints

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kcrud.access.system.SessionContext
import kcrud.base.persistence.pagination.Page
import kcrud.base.persistence.pagination.Pageable
import kcrud.base.persistence.pagination.getPageable
import kcrud.domain.employee.entities.EmployeeEntity
import kcrud.domain.employee.routing.annotation.EmployeeRouteAPI
import kcrud.domain.employee.service.EmployeeService
import org.koin.core.parameter.parametersOf
import org.koin.ktor.plugin.scope

@EmployeeRouteAPI
internal fun Route.findAllEmployees() {
    // Find all employees.
    get {
        val sessionContext: SessionContext? = call.principal<SessionContext>()
        val service: EmployeeService = call.scope.get<EmployeeService> { parametersOf(sessionContext) }
        val pageable: Pageable? = call.getPageable()
        val employees: Page<EmployeeEntity> = service.findAll(pageable = pageable)
        call.respond(status = HttpStatusCode.OK, message = employees)
    }
}
