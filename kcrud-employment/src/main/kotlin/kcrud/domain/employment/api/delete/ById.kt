/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.domain.employment.api.delete

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import kcrud.core.env.SessionContext.Companion.getContext
import kcrud.core.persistence.utils.toUuid
import kcrud.domain.employment.api.EmploymentRouteAPI
import kcrud.domain.employment.service.EmploymentService
import org.koin.core.parameter.parametersOf
import org.koin.ktor.plugin.scope
import kotlin.uuid.Uuid

@EmploymentRouteAPI
internal fun Route.deleteEmploymentByIdRoute() {
    /**
     * Delete an employment by ID.
     * @OpenAPITag Employment
     */
    delete("v1/employees/{employee_id}/employments/{employment_id}") {
        val employmentId: Uuid = call.parameters.getOrFail(name = "employment_id").toUuid()
        val service: EmploymentService = call.scope.get<EmploymentService> { parametersOf(call.getContext()) }
        val deletedCount: Int = service.delete(employmentId = employmentId)
        call.respond(status = HttpStatusCode.OK, message = deletedCount)
    }
}
