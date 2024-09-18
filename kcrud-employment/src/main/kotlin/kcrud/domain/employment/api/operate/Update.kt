/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.domain.employment.api.operate

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import kcrud.base.env.CallContext.Companion.getContext
import kcrud.base.persistence.utils.toUuid
import kcrud.domain.employment.api.EmploymentRouteAPI
import kcrud.domain.employment.errors.EmploymentError
import kcrud.domain.employment.model.Employment
import kcrud.domain.employment.model.EmploymentRequest
import kcrud.domain.employment.service.EmploymentService
import org.koin.core.parameter.parametersOf
import org.koin.ktor.plugin.scope
import kotlin.uuid.Uuid

@EmploymentRouteAPI
internal fun Route.updateEmploymentByIdRoute() {
    /**
     * Update an employment by ID.
     * @OpenAPITag Employment
     */
    put("v1/employees/{employee_id}/employments/{employment_id}") {
        val employeeId: Uuid = call.parameters.getOrFail(name = "employee_id").toUuid()
        val employmentId: Uuid = call.parameters.getOrFail(name = "employment_id").toUuid()
        val request: EmploymentRequest = call.receive<EmploymentRequest>()

        val service: EmploymentService = call.scope.get<EmploymentService> { parametersOf(call.getContext()) }
        val employment: Employment? = service.update(
            employeeId = employeeId,
            employmentId = employmentId,
            request = request
        ).getOrThrow()

        if (employment == null) {
            throw EmploymentError.EmploymentNotFound(employeeId = employeeId, employmentId = employmentId)
        } else {
            call.respond(status = HttpStatusCode.OK, message = employment)
        }
    }
}
