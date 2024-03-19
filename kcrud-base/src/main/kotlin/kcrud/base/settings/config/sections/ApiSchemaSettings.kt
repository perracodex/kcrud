/*
 * Copyright (c) 2024-Present Perraco. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.settings.config.sections

import kcrud.base.env.EnvironmentType
import kcrud.base.settings.config.parser.IConfigSection

/**
 * Contains settings related to Swagger and OpenAPI.
 *
 * @property environments The list of environments under which the documentation is enabled.
 * @property schemaRoot The root endpoint path for API schema documentation.
 * @property schemaResourceFile The OpenAPI resource documentation file.
 * @property openApiEndpoint The endpoint to the OpenAPI specification.
 * @property swaggerEndpoint The endpoint to the Swagger UI.
 * @property redocEndpoint The endpoint path to the Redoc UI.
 */
data class ApiSchemaSettings(
    val environments: List<EnvironmentType>,
    val schemaRoot: String,
    val schemaResourceFile: String,
    val openApiEndpoint: String,
    val swaggerEndpoint: String,
    val redocEndpoint: String
) : IConfigSection
