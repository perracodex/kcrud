/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.core.plugins

import io.github.perracodex.kopapi.plugin.Kopapi
import io.github.perracodex.kopapi.type.*
import io.ktor.server.application.*
import kcrud.core.settings.AppSettings

/**
 * Configures OpenAPI, Swagger-UI and Redoc.
 *
 * #### References
 * - [Kopapi Documentation](https://github.com/perracodex/kopapi)
 */
public fun Application.configureApiSchema() {

    if (!AppSettings.apiSchema.environments.contains(AppSettings.runtime.environment)) {
        return
    }

    install(plugin = Kopapi) {
        enabled = true
        onDemand = false
        logPluginRoutes = true

        apiDocs {
            openApiUrl = AppSettings.apiSchema.openApiEndpoint
            openApiFormat = OpenApiFormat.YAML
            redocUrl = AppSettings.apiSchema.redocEndpoint

            swagger {
                url = AppSettings.apiSchema.swaggerEndpoint
                persistAuthorization = true
                withCredentials = false
                docExpansion = SwaggerDocExpansion.NONE
                displayRequestDuration = true
                displayOperationId = true
                operationsSorter = SwaggerOperationsSorter.UNSORTED
                uiTheme = SwaggerUiTheme.DARK
                syntaxTheme = SwaggerSyntaxTheme.NORD
                includeErrors = true
            }
        }

        info {
            title = "KCRUD API"
            version = "1.0.0"
            description = "KCRUD API Documentation"
            license {
                name = "MIT"
                url = "https://opensource.org/licenses/MIT"
            }
        }

        servers {
            add(urlString = "{protocol}://{host}:{port}") {
                description = "KCRUD API Server"
                variable(name = "protocol", defaultValue = "http") {
                    choices = setOf("http", "https")
                }
                variable(name = "host", defaultValue = "localhost") {
                    choices = setOf(AppSettings.deployment.host, "localhost")
                }
                variable(name = "port", defaultValue = "8080") {
                    choices = setOf(
                        AppSettings.deployment.port.toString(),
                        AppSettings.deployment.sslPort.toString(),
                    )
                }
            }
        }

        tags {
            add(name = "RBAC", description = "Role-Based Access Control")
            add(name = "Token", description = "JWT Token Management")
            add(name = "Events", description = "System Events")
            add(name = "System", description = "System Management")
            add(name = "Scheduler", description = "Scheduler Management")
            add(name = "Scheduler Admin", description = "Scheduler Maintenance")
            add(name = "Employee", description = "Employee Management")
            add(name = "Employment", description = "Employment Management")
            add(name = "Demo", description = "Demo related APIs")
        }
    }
}
