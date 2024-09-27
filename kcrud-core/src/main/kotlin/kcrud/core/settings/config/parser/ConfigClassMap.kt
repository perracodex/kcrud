/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.core.settings.config.parser

import kcrud.core.settings.annotation.ConfigurationAPI
import kcrud.core.settings.config.ConfigurationCatalog
import kcrud.core.settings.config.sections.DeploymentSettings
import kotlin.reflect.KClass

/**
 * Represents a mapping between a configuration path in the configuration file
 * and a corresponding data class to which the configuration values will be mapped.
 *
 * Each instance of this class defines how a specific section of the configuration
 * file is mapped to a property within the [ConfigurationCatalog].
 *
 * @property mappingName The name of the property in the [ConfigurationCatalog] where to map into.
 * @property path The hierarchical path in the configuration file (e.g., `"ktor.deployment"`) from which the settings are loaded.
 * @property kClass The [KClass] of the data class that represents the configuration section (e.g., [DeploymentSettings]).
 *
 * @see [ConfigurationCatalog]
 */
@ConfigurationAPI
internal data class ConfigClassMap<T : IConfigSection>(
    val mappingName: String,
    val path: String,
    val kClass: KClass<T>
)
