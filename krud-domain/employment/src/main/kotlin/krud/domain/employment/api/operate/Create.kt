/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package krud.domain.employment.api.operate

import io.github.perracodex.kopapi.dsl.operation.api
import io.github.perracodex.kopapi.dsl.parameter.pathParameter
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import krud.base.context.getContext
import krud.base.error.AppException
import krud.base.util.toUuid
import krud.domain.employment.api.EmploymentRouteApi
import krud.domain.employment.error.EmploymentError
import krud.domain.employment.model.Employment
import krud.domain.employment.model.EmploymentRequest
import krud.domain.employment.service.EmploymentService
import org.koin.core.parameter.parametersOf
import org.koin.ktor.plugin.scope
import kotlin.uuid.Uuid

@EmploymentRouteApi
internal fun Route.createEmploymentRoute() {
    post("/api/v1/employees/{employee_id}/employments") {
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
    } api {
        tags = setOf("Employment")
        summary = "Create a new employment."
        description = "Create a new employment for an employee."
        operationId = "createEmployment"
        pathParameter<Uuid>(name = "employee_id") {
            description = "The employee ID to create an employment for."
        }
        requestBody<EmploymentRequest> {
            description = "The employment request."
        }
        response<Employment>(status = HttpStatusCode.Created) {
            description = "The employment created."
        }
        response<AppException.ErrorResponse>(status = EmploymentError.EmployeeNotFound.STATUS_CODE) {
            description = "Employee not found. Code: ${EmploymentError.EmployeeNotFound.ERROR_CODE}"
        }
        bearerSecurity(name = "Authentication") {
            description = "Access to employee data."
        }
    }
}
