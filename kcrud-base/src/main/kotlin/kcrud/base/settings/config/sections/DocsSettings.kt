/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.settings.config.sections

import kcrud.base.infrastructure.env.EnvironmentType
import kcrud.base.settings.config.parser.IConfigSection

/**
 * Contains settings related to Swagger, OpenAPI, and Redoc.
 *
 * @property environments The list of environments under which the documentation is enabled.
 * @property yamlFile The documentation location file.
 * @property swaggerPath The path to the Swagger UI.
 * @property openApiPath The path to the OpenAPI specification.
 * @property redocPath The path to the Redoc file.
 */
data class DocsSettings(
    val environments: List<EnvironmentType>,
    val yamlFile: String,
    val swaggerPath: String,
    val openApiPath: String,
    val redocPath: String
) : IConfigSection
