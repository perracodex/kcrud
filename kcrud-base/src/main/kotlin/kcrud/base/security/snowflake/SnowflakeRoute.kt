/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.security.snowflake

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import kcrud.base.settings.AppSettings

/**
 * Defines the snowflake route, which is used to parse snowflake IDs.
 */
public fun Route.snowflakeRoute() {
    authenticate(AppSettings.security.basicAuth.providerName, optional = !AppSettings.security.isEnabled) {
        /**
         * Snowflake parser to read back the components of a snowflake ID.
         * @OpenAPITag System
         */
        get("/snowflake/{id}") {
            val snowflakeId: String = call.parameters.getOrFail(name = "id")
            val data: SnowflakeData = SnowflakeFactory.parse(id = snowflakeId)
            call.respond(status = HttpStatusCode.OK, message = data)
        }
    }
}
