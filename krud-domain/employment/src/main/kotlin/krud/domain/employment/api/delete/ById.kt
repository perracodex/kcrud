/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package krud.domain.employment.api.delete

import io.github.perracodex.kopapi.dsl.operation.api
import io.github.perracodex.kopapi.dsl.parameter.pathParameter
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import krud.base.context.getContext
import krud.base.util.toUuid
import krud.domain.employment.api.EmploymentRouteApi
import krud.domain.employment.service.EmploymentService
import org.koin.core.parameter.parametersOf
import org.koin.ktor.plugin.scope
import kotlin.uuid.Uuid

@EmploymentRouteApi
internal fun Route.deleteEmploymentByIdRoute() {
    delete("/api/v1/employees/{employee_id}/employments/{employment_id}") {
        val employmentId: Uuid = call.parameters.getOrFail(name = "employment_id").toUuid()
        val service: EmploymentService = call.scope.get<EmploymentService> { parametersOf(call.getContext()) }
        val deletedCount: Int = service.delete(employmentId = employmentId)
        call.respond(status = HttpStatusCode.OK, message = deletedCount)
    } api {
        tags = setOf("Employment")
        summary = "Delete an employment by ID."
        description = "Delete an employment by its unique ID."
        operationId = "deleteEmploymentById"
        pathParameter<Uuid>(name = "employee_id") {
            description = "The employee ID to delete employments for."
        }
        pathParameter<Uuid>(name = "employment_id") {
            description = "The employment ID to delete."
        }
        response<Int>(status = HttpStatusCode.OK) {
            description = "The number of employments deleted."
        }
        bearerSecurity(name = "Authentication") {
            description = "Access to employee data."
        }
    }
}
