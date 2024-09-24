/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.core.settings.config

import kcrud.core.settings.config.sections.*
import kcrud.core.settings.config.sections.security.SecuritySettings
import kotlinx.serialization.Serializable

/**
 * Represents the top-level configuration settings for the application.
 *
 * This data class serves as a structured representation of the application's configuration file.
 * Each property in this class corresponds to a specific section in the configuration file,
 * allowing for a direct and type-safe mapping of configuration data.
 *
 * Note: It is crucial that the property names in this class exactly match the respective section
 * names in the configuration file to ensure proper mapping and instantiation of the settings.
 *
 * @property apiSchema The API schema settings for the application.
 * @property cors The CORS settings for the application.
 * @property database The database settings for the application.
 * @property deployment The deployment settings for the application.
 * @property runtime The runtime settings for the application.
 * @property security The security settings for the application.
 */
@Serializable
public data class ConfigurationCatalog(
    val apiSchema: ApiSchemaSettings,
    val cors: CorsSettings,
    val database: DatabaseSettings,
    val deployment: DeploymentSettings,
    val runtime: RuntimeSettings,
    val security: SecuritySettings
)
