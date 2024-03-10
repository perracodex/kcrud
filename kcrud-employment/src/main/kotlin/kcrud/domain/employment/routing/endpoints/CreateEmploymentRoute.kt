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
import kcrud.base.infrastructure.env.SessionContext
import kcrud.base.persistence.utils.toUUID
import kcrud.domain.employment.entities.EmploymentEntity
import kcrud.domain.employment.entities.EmploymentRequest
import kcrud.domain.employment.routing.annotation.EmploymentRouteAPI
import kcrud.domain.employment.service.EmploymentService
import org.koin.core.parameter.parametersOf
import org.koin.ktor.plugin.scope
import java.util.*

@EmploymentRouteAPI
internal fun Route.createEmployment() {
    // Create a new employment.
    post<EmploymentRequest> { employmentRequest ->
        val employeeId: UUID = call.parameters["employee_id"].toUUID()

        val sessionContext: SessionContext? = call.principal<SessionContext>()
        val service: EmploymentService = call.scope.get<EmploymentService> { parametersOf(sessionContext) }
        val newEmployment: EmploymentEntity = service.create(
            employeeId = employeeId,
            employmentRequest = employmentRequest
        )

        call.respond(status = HttpStatusCode.Created, message = newEmployment)
    }
}
