/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.settings.config

import kcrud.base.settings.config.sections.*
import kcrud.base.settings.config.sections.security.SecuritySettings

/**
 * Represents the top-level configuration settings for the application.
 *
 * This data class serves as a structured representation of the application's configuration file.
 * Each property in this class corresponds to a specific section in the configuration file,
 * allowing for a direct and type-safe mapping of configuration data.
 *
 * Note: It is crucial that the property names in this class exactly match the respective section
 * names in the configuration file to ensure proper mapping and instantiation of the settings.
 */
data class ConfigurationCatalog(
    val runtime: RuntimeSettings,
    val deployment: DeploymentSettings,
    val cors: CorsSettings,
    val database: DatabaseSettings,
    val docs: DocsSettings,
    val security: SecuritySettings
)
