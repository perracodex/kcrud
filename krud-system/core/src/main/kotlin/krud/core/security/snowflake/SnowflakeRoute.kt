/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package krud.core.security.snowflake

import io.github.perracodex.kopapi.dsl.operation.api
import io.github.perracodex.kopapi.dsl.parameter.pathParameter
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import krud.core.settings.AppSettings

/**
 * Defines the snowflake route, which is used to parse snowflake IDs.
 */
public fun Route.snowflakeRoute() {
    authenticate(AppSettings.security.basicAuth.providerName, optional = !AppSettings.security.isEnabled) {
        get("/admin/snowflake/{id}") {
            val snowflakeId: String = call.parameters.getOrFail(name = "id")
            val data: SnowflakeData = SnowflakeFactory.parse(id = snowflakeId)
            call.respond(status = HttpStatusCode.OK, message = data)
        } api {
            tags = setOf("System")
            summary = "Snowflake parser."
            description = "Reads back the components of a snowflake ID."
            operationId = "parseSnowflake"
            pathParameter<String>(name = "id") {
                description = "The snowflake ID to parse."
            }
            response<SnowflakeData>(status = HttpStatusCode.OK) {
                description = "The parsed snowflake data."
            }
            basicSecurity(name = "System") {
                description = "Access to system information."
            }
        }
    }
}
