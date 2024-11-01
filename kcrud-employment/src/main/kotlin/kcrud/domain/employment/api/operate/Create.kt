/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.domain.employment.api.operate

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import kcrud.core.context.getContext
import kcrud.core.persistence.utils.toUuid
import kcrud.domain.employment.api.EmploymentRouteApi
import kcrud.domain.employment.errors.EmploymentError
import kcrud.domain.employment.model.Employment
import kcrud.domain.employment.model.EmploymentRequest
import kcrud.domain.employment.service.EmploymentService
import org.koin.core.parameter.parametersOf
import org.koin.ktor.plugin.scope
import kotlin.uuid.Uuid

@EmploymentRouteApi
internal fun Route.createEmploymentRoute() {
    /**
     * Create a new employment.
     * @OpenAPITag Employment
     */
    post("v1/employees/{employee_id}/employments") {
        val employeeId: Uuid = call.parameters.getOrFail(name = "employee_id").toUuid()
        val request: EmploymentRequest = call.receive<EmploymentRequest>()

        val service: EmploymentService = call.scope.get<EmploymentService> { parametersOf(call.getContext()) }
        val employment: Employment? = service.create(
            employeeId = employeeId,
            request = request
        ).getOrThrow()

        if (employment == null) {
            throw EmploymentError.EmployeeNotFound(employeeId = employeeId)
        } else {
            call.respond(status = HttpStatusCode.Created, message = employment)
        }
    }
}
