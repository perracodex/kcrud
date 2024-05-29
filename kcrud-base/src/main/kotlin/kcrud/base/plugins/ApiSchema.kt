/*
 * Copyright (c) 2024-Present Perracodex. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.plugins

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.openapi.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*
import io.swagger.codegen.v3.generators.html.StaticHtmlCodegen
import kcrud.base.settings.AppSettings

/**
 * Configures OpenAPI, Swagger-UI and Redoc.
 *
 * See [OpenAPI](https://ktor.io/docs/server-openapi.html)
 *
 * See [OpenAPI Generation](https://www.jetbrains.com/help/idea/ktor.html#openapi)
 *
 * See [Swagger-UI](https://ktor.io/docs/server-swagger-ui.html)
 *
 * See [Redoc](https://swagger.io/blog/api-development/redoc-openapi-powered-documentation/)
 */
fun Application.configuredApiSchema() {

    if (!AppSettings.apiSchema.environments.contains(AppSettings.runtime.environment)) {
        return
    }

    routing {
        // Serve the static files: OpenAPI YAML, Redoc HTML, etc.
        staticResources(remotePath = AppSettings.apiSchema.schemaRoot, basePackage = "openapi")

        // OpenAPI.
        openAPI(
            path = AppSettings.apiSchema.openApiEndpoint,
            swaggerFile = AppSettings.apiSchema.schemaResourceFile
        ) {
            codegen = StaticHtmlCodegen()
        }

        // Swagger-UI.
        swaggerUI(
            path = AppSettings.apiSchema.swaggerEndpoint,
            swaggerFile = AppSettings.apiSchema.schemaResourceFile
        ) {
            version = "5.11.8"
        }
    }
}
