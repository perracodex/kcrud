/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.server.demo.api.dashboard

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.perracodex.exposed.pagination.Page
import io.perracodex.exposed.pagination.getPageable
import kcrud.core.context.getContext
import kcrud.domain.employment.model.Employment
import kcrud.domain.employment.service.EmploymentService
import kcrud.server.demo.DemoAPI
import org.koin.core.parameter.parametersOf
import org.koin.ktor.plugin.scope

@DemoAPI
internal fun Route.dumpJsonRoute() {
    /**
     * Return all demo records as JSON.
     * @OpenAPITag Demo
     */
    get("demo/json") {
        val service: EmploymentService = call.scope.get<EmploymentService> { parametersOf(call.getContext()) }
        val page: Page<Employment> = service.findAll(pageable = call.getPageable())
        call.respond(status = HttpStatusCode.OK, message = page)
    }
}
