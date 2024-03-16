/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
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
 * Configures Swagger-UI, OpenAPI and Redoc.
 *
 * See [OpenAPI](https://ktor.io/docs/openapi.html)
 *
 * See [OpenAPI Generation](https://www.jetbrains.com/help/idea/ktor.html#openapi)
 *
 * See [Swagger-UI](https://ktor.io/docs/swagger-ui.html#configure-swagger)
 *
 * See [Redoc](https://swagger.io/blog/api-development/redoc-openapi-powered-documentation/)
 */
fun Application.configuredDocumentation() {

    if (!AppSettings.docs.environments.contains(AppSettings.runtime.environment)) {
        return
    }

    routing {
        val yamlFile: String = AppSettings.docs.yamlFile
        val rootPath = "v1"

        // Root path.
        staticResources(remotePath = rootPath, basePackage = "openapi")

        // OpenAPI.
        val openApiPath = AppSettings.docs.openApiPath
        openAPI(path = openApiPath, swaggerFile = yamlFile) {
            codegen = StaticHtmlCodegen()
        }

        // Swagger-UI.
        val swaggerPath = AppSettings.docs.swaggerPath
        swaggerUI(path = swaggerPath, swaggerFile = yamlFile) {
            version = "5.11.8"
        }
    }
}
