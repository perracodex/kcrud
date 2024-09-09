/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.server.demo.routing.view

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kcrud.base.env.SessionContext
import kcrud.base.persistence.pagination.Page
import kcrud.base.persistence.pagination.Pageable
import kcrud.base.persistence.pagination.getPageable
import kcrud.domain.employment.entity.EmploymentEntity
import kcrud.domain.employment.service.EmploymentService
import kcrud.server.demo.DemoAPI
import org.koin.core.parameter.parametersOf
import org.koin.ktor.plugin.scope

@DemoAPI
internal fun Route.dumpJsonRoute() {
    // Return all demo records as JSON.
    get("json") {
        val sessionContext: SessionContext? = SessionContext.from(call = call)
        val service: EmploymentService = call.scope.get<EmploymentService> { parametersOf(sessionContext) }
        val pageable: Pageable? = call.getPageable()
        val page: Page<EmploymentEntity> = service.findAll(pageable = pageable)
        call.respond(status = HttpStatusCode.OK, message = page)
    }
}
