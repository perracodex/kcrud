/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.domain.employment.routing.endpoints.operate

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kcrud.base.env.SessionContext
import kcrud.base.persistence.utils.toUUID
import kcrud.domain.employment.entity.EmploymentEntity
import kcrud.domain.employment.entity.EmploymentRequest
import kcrud.domain.employment.errors.EmploymentError
import kcrud.domain.employment.routing.annotation.EmploymentRouteAPI
import kcrud.domain.employment.service.EmploymentService
import org.koin.core.parameter.parametersOf
import org.koin.ktor.plugin.scope
import java.util.*

@EmploymentRouteAPI
internal fun Route.updateEmploymentById() {
    // Update an employment by ID.
    put<EmploymentRequest> { request ->
        val employeeId: UUID = call.parameters["employee_id"].toUUID()
        val employmentId: UUID = call.parameters["employment_id"].toUUID()

        val sessionContext: SessionContext? = call.principal<SessionContext>()
        val service: EmploymentService = call.scope.get<EmploymentService> { parametersOf(sessionContext) }
        val updatedEmployment: EmploymentEntity? = service.update(
            employeeId = employeeId,
            employmentId = employmentId,
            employmentRequest = request
        )

        updatedEmployment?.let {
            call.respond(status = HttpStatusCode.OK, message = updatedEmployment)
        } ?: EmploymentError.EmploymentNotFound(
            employeeId = employeeId,
            employmentId = employmentId
        ).raise()
    }
}
