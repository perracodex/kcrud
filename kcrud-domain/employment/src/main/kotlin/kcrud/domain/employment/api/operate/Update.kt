/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.domain.employment.api.operate

import io.github.perracodex.kopapi.dsl.operation.api
import io.github.perracodex.kopapi.dsl.parameter.pathParameter
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import kcrud.core.context.getContext
import kcrud.core.error.AppException
import kcrud.core.util.toUuid
import kcrud.domain.employment.api.EmploymentRouteApi
import kcrud.domain.employment.error.EmploymentError
import kcrud.domain.employment.model.Employment
import kcrud.domain.employment.model.EmploymentRequest
import kcrud.domain.employment.service.EmploymentService
import org.koin.core.parameter.parametersOf
import org.koin.ktor.plugin.scope
import kotlin.uuid.Uuid

@EmploymentRouteApi
internal fun Route.updateEmploymentByIdRoute() {
    put("/api/v1/employees/{employee_id}/employments/{employment_id}") {
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
    } api {
        tags = setOf("Employment")
        summary = "Update an employment by ID."
        description = "Update an employment's details by their unique ID."
        operationId = "updateEmploymentById"
        pathParameter<Uuid>(name = "employee_id") {
            description = "The employee ID to update employments for."
        }
        pathParameter<Uuid>(name = "employment_id") {
            description = "The employment ID to update."
        }
        requestBody<EmploymentRequest> {
            description = "The employment request."
        }
        response<Employment>(status = HttpStatusCode.OK) {
            description = "The employment updated."
        }
        response<AppException.ErrorResponse>(status = EmploymentError.EmploymentNotFound.STATUS_CODE) {
            description = "Employment not found. Code: ${EmploymentError.EmploymentNotFound.ERROR_CODE}"
        }
        bearerSecurity(name = "Authentication") {
            description = "Access to employee data."
        }
    }
}
