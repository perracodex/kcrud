/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.settings.config.parser

import kcrud.base.settings.annotation.ConfigurationAPI
import kcrud.base.settings.config.ConfigurationCatalog
import kcrud.base.settings.config.sections.DeploymentSettings
import kotlin.reflect.KClass

/**
 * Maps a configuration path to a data class type.
 *
 * @param path The section in the configuration file, i.e. "ktor.deployment".
 * @param argument The corresponding name of the property in the [ConfigurationCatalog] class,
 *                 to which the configuration values will be mapped.
 * @param kClass The target data class type which will map the configuration, i.e. [DeploymentSettings].
 * @return A [ConfigClassMap] object containing the mapping.
 * @see ConfigurationCatalog
 */
@ConfigurationAPI
data class ConfigClassMap<T : IConfigSection>(
    val path: String,
    val argument: String,
    val kClass: KClass<T>
)
