/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.admin.system

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kcrud.base.admin.rbac.plugin.withRbac
import kcrud.base.database.schema.admin.rbac.types.RbacAccessLevel
import kcrud.base.database.schema.admin.rbac.types.RbacResource
import kcrud.base.infrastructure.env.SessionContext
import kcrud.base.infrastructure.utils.NetworkUtils
import kcrud.base.security.snowflake.SnowflakeData
import kcrud.base.security.snowflake.SnowflakeFactory
import kcrud.base.settings.AppSettings

/**
 * Defines common system endpoints.
 */
fun Route.systemRoute() {

    // Server root endpoint.
    get("/") {
        val sessionContext: SessionContext? = call.principal<SessionContext>()
        sessionContext?.let {
            call.respondText(text = "Hello World. Welcome ${it.username}!")
        } ?: call.respondText(text = "Hello World.")
    }

    authenticate(AppSettings.security.basic.providerName, optional = !AppSettings.security.isEnabled) {
        withRbac(resource = RbacResource.SYSTEM, accessLevel = RbacAccessLevel.FULL) {
            // Snowflake parser to read back the components of a snowflake ID.
            get("/snowflake/{id}") {
                val data: SnowflakeData = SnowflakeFactory.parse(id = call.parameters["id"]!!)
                call.respond(status = HttpStatusCode.OK, message = data)
            }
        }
    }

    NetworkUtils.logEndpoints(
        reason = "System endpoints",
        endpoints = listOf(
            "snowflake/${SnowflakeFactory.nextId()}"
        )
    )
}
