/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.core.settings.config.parser

import kcrud.core.settings.annotation.ConfigurationAPI
import kcrud.core.settings.config.ConfigurationCatalog
import kcrud.core.settings.config.sections.DeploymentSettings
import kotlin.reflect.KClass

/**
 * Maps a configuration path to a data class type.
 *
 * @property mappingName The corresponding name of the property in the [ConfigurationCatalog] class,
 *                       to which the configuration values will be mapped to.
 * @property path The section in the configuration file, i.e. "ktor.deployment".
 * @property kClass The target data class type which will map the configuration, i.e. [DeploymentSettings].
 *
 * @see ConfigurationCatalog
 */
@ConfigurationAPI
internal data class ConfigClassMap<T : IConfigSection>(
    val mappingName: String,
    val path: String,
    val kClass: KClass<T>
)
