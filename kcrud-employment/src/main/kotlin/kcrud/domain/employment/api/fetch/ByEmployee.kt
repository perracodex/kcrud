/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.domain.employment.api.fetch

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import kcrud.base.env.SessionContext
import kcrud.base.persistence.utils.toUuid
import kcrud.domain.employment.api.EmploymentRouteAPI
import kcrud.domain.employment.model.Employment
import kcrud.domain.employment.service.EmploymentService
import org.koin.core.parameter.parametersOf
import org.koin.ktor.plugin.scope
import kotlin.uuid.Uuid

@EmploymentRouteAPI
internal fun Route.findEmploymentByEmployeeIdRoute() {
    /**
     * Find all employments for an employee ID.
     * @OpenAPITag Employment
     */
    get("v1/employees/{employee_id}/employments") {
        val employeeId: Uuid = call.parameters.getOrFail(name = "employee_id").toUuid()

        val sessionContext: SessionContext? = SessionContext.from(call = call)
        val service: EmploymentService = call.scope.get<EmploymentService> { parametersOf(sessionContext) }

        val employments: List<Employment> = service.findByEmployeeId(employeeId = employeeId)
        call.respond(status = HttpStatusCode.OK, message = employments)
    }
}
