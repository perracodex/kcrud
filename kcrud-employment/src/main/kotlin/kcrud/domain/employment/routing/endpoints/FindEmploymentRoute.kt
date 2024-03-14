/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.domain.employment.routing.endpoints

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kcrud.access.system.SessionContext
import kcrud.base.persistence.utils.toUUID
import kcrud.domain.employment.entities.EmploymentEntity
import kcrud.domain.employment.errors.EmploymentError
import kcrud.domain.employment.routing.annotation.EmploymentRouteAPI
import kcrud.domain.employment.service.EmploymentService
import org.koin.core.parameter.parametersOf
import org.koin.ktor.plugin.scope
import java.util.*

@EmploymentRouteAPI
internal fun Route.findEmploymentByEmployeeId() {
    // Find all employments for an employee ID.
    get {
        val employeeId: UUID = call.parameters["employee_id"].toUUID()

        val sessionContext: SessionContext? = call.principal<SessionContext>()
        val service: EmploymentService = call.scope.get<EmploymentService> { parametersOf(sessionContext) }
        val employments: List<EmploymentEntity> = service.findByEmployeeId(employeeId = employeeId)

        call.respond(status = HttpStatusCode.OK, message = employments)
    }
}

@EmploymentRouteAPI
internal fun Route.findEmploymentById() {
    // Find an employment by ID.
    get {
        val employeeId: UUID = call.parameters["employee_id"].toUUID()
        val employmentId: UUID = call.parameters["employment_id"].toUUID()

        val sessionContext: SessionContext? = call.principal<SessionContext>()
        val service: EmploymentService = call.scope.get<EmploymentService> { parametersOf(sessionContext) }
        val employment: EmploymentEntity? = service.findById(
            employeeId = employeeId,
            employmentId = employmentId
        )

        if (employment != null) {
            call.respond(status = HttpStatusCode.OK, message = employment)
        } else {
            EmploymentError.EmploymentNotFound(
                employeeId = employeeId,
                employmentId = employmentId
            ).raise()
        }
    }
}
