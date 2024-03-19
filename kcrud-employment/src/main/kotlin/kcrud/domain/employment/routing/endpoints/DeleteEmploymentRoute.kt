/*
 * Copyright (c) 2024-Present Perraco. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.domain.employment.routing.endpoints

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kcrud.base.env.SessionContext
import kcrud.base.persistence.utils.toUUID
import kcrud.domain.employment.routing.annotation.EmploymentRouteAPI
import kcrud.domain.employment.service.EmploymentService
import org.koin.core.parameter.parametersOf
import org.koin.ktor.plugin.scope
import java.util.*

@EmploymentRouteAPI
internal fun Route.deleteEmploymentByEmployeeId() {
    // Delete all employments for an employee ID.
    delete {
        val employeeId: UUID = call.parameters["employee_id"].toUUID()

        val sessionContext: SessionContext? = call.principal<SessionContext>()
        val service: EmploymentService = call.scope.get<EmploymentService> { parametersOf(sessionContext) }
        val deletedCount: Int = service.deleteAll(employeeId = employeeId)

        call.respond(status = HttpStatusCode.OK, message = deletedCount)
    }
}

@EmploymentRouteAPI
internal fun Route.deleteEmploymentById() {
    // Delete an employment by ID.
    delete {
        val employmentId: UUID = call.parameters["employment_id"].toUUID()

        val sessionContext: SessionContext? = call.principal<SessionContext>()
        val service: EmploymentService = call.scope.get<EmploymentService> { parametersOf(sessionContext) }
        val deletedCount: Int = service.delete(employmentId = employmentId)

        call.respond(status = HttpStatusCode.OK, message = deletedCount)
    }
}
