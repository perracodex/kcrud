/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package krud.domain.employment.api.fetch

import io.github.perracodex.kopapi.dsl.operation.api
import io.github.perracodex.kopapi.dsl.parameter.pathParameter
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import krud.base.context.getContext
import krud.base.util.toUuid
import krud.domain.employment.api.EmploymentRouteApi
import krud.domain.employment.model.Employment
import krud.domain.employment.service.EmploymentService
import org.koin.core.parameter.parametersOf
import org.koin.ktor.plugin.scope
import kotlin.uuid.Uuid

@EmploymentRouteApi
internal fun Route.findEmploymentByEmployeeIdRoute() {
    get("/api/v1/employees/{employee_id}/employments") {
        val employeeId: Uuid = call.parameters.getOrFail(name = "employee_id").toUuid()
        val service: EmploymentService = call.scope.get<EmploymentService> { parametersOf(call.getContext()) }
        val employments: List<Employment> = service.findByEmployeeId(employeeId = employeeId)
        call.respond(status = HttpStatusCode.OK, message = employments)
    } api {
        tags = setOf("Employment")
        summary = "Find all employments for an employee ID."
        description = "Retrieve all employments for an employee by their unique ID."
        operationId = "findEmploymentByEmployeeId"
        pathParameter<Uuid>(name = "employee_id") {
            description = "The employee ID to find employments for."
        }
        response<List<Employment>>(status = HttpStatusCode.OK) {
            description = "The employments found."
        }
        bearerSecurity(name = "Authentication") {
            description = "Access to employee data."
        }
    }
}
