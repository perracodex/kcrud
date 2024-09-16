/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.domain.employment.routing.endpoints.get

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import kcrud.base.env.SessionContext
import kcrud.base.persistence.utils.toUuid
import kcrud.domain.employment.model.Employment
import kcrud.domain.employment.routing.annotation.EmploymentRouteAPI
import kcrud.domain.employment.service.EmploymentService
import org.koin.core.parameter.parametersOf
import org.koin.ktor.plugin.scope
import kotlin.uuid.Uuid

@EmploymentRouteAPI
internal fun Route.findEmploymentByIdRoute() {
    // Find an employment by ID.
    get("v1/employees/{employee_id}/employments/{employment_id}") {
        val employmentId: Uuid = call.parameters.getOrFail(name = "employment_id").toUuid()
        val employeeId: Uuid = call.parameters.getOrFail(name = "employee_id").toUuid()

        val sessionContext: SessionContext? = SessionContext.from(call = call)
        val service: EmploymentService = call.scope.get<EmploymentService> { parametersOf(sessionContext) }

        val employment: Employment = service.findByIdOrThrow(
            employeeId = employeeId,
            employmentId = employmentId
        )

        call.respond(status = HttpStatusCode.OK, message = employment)
    }
}
