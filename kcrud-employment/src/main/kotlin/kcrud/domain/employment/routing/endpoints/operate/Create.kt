/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.domain.employment.routing.endpoints.operate

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kcrud.base.env.SessionContext
import kcrud.base.persistence.utils.toUuid
import kcrud.domain.employment.model.EmploymentDto
import kcrud.domain.employment.model.EmploymentRequest
import kcrud.domain.employment.routing.annotation.EmploymentRouteAPI
import kcrud.domain.employment.service.EmploymentService
import org.koin.core.parameter.parametersOf
import org.koin.ktor.plugin.scope
import kotlin.uuid.Uuid

@EmploymentRouteAPI
internal fun Route.createEmployment() {
    // Create a new employment.
    post<EmploymentRequest> { request ->
        val employeeId: Uuid = call.parameters["employee_id"].toUuid()

        val sessionContext: SessionContext? = SessionContext.from(call = call)
        val service: EmploymentService = call.scope.get<EmploymentService> { parametersOf(sessionContext) }
        val newEmployment: EmploymentDto = service.create(
            employeeId = employeeId,
            employmentRequest = request
        )

        call.respond(status = HttpStatusCode.Created, message = newEmployment)
    }
}
