/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package krud.server.demo.api.operate

import io.github.perracodex.kopapi.dsl.operation.api
import io.github.perracodex.kopapi.dsl.parameter.queryParameter
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import krud.base.context.SessionContext
import krud.base.context.getContext
import krud.domain.employee.service.EmployeeService
import krud.domain.employment.service.EmploymentService
import krud.server.demo.DemoApi
import krud.server.demo.DemoUtils
import org.koin.core.parameter.parametersOf
import org.koin.ktor.plugin.scope

@DemoApi
internal fun Route.createRecordsRoute() {
    @Suppress("MagicNumber")
    val maxAllowedBatch = 100_000

    post("/demo") {
        val count: Int = call.request.queryParameters.getOrFail<Int>(name = "count")

        if (count in 1..maxAllowedBatch) {
            val sessionContext: SessionContext = call.getContext()
            val employeeService: EmployeeService = call.scope.get<EmployeeService> { parametersOf(sessionContext) }
            val employmentService: EmploymentService = call.scope.get<EmploymentService> { parametersOf(sessionContext) }

            DemoUtils.createDemoRecords(
                employeeService = employeeService,
                employmentService = employmentService,
                count = count
            )
            call.respond(status = HttpStatusCode.OK, message = "Created $count employees.")
        } else {
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = "Invalid count. Must be between 1 and $maxAllowedBatch."
            )
        }
    } api {
        tags = setOf("Demo")
        summary = "Create a batch of demo records."
        description = "Create a batch of demo records in the system."
        operationId = "createRecords"
        queryParameter<Int>(name = "count") {
            description = "Number of records to create."
        }
        response<String>(status = HttpStatusCode.OK) {
            description = "Message indicating the number of employees created."
        }
        response(status = HttpStatusCode.BadRequest) {
            description = "Invalid count. Must be between 1 and $maxAllowedBatch."
        }
        basicSecurity(name = "Demo Authentication") {
            description = "Access to demo data."
        }
    }
}
