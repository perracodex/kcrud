/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.core.settings.parser

import kcrud.core.settings.annotation.ConfigurationAPI
import kotlin.reflect.KClass

/**
 * Represents a mapping between a configuration path in the configuration file
 * and a corresponding data class to which the configuration values will be mapped.
 *
 * Each instance of this class defines how a specific section of the configuration
 * file is mapped to a property within the [IConfigCatalog].
 *
 * @property keyPath The hierarchical key-path in the configuration file from which to parse, (e.g., `"ktor.deployment"`).
 * @property catalogProperty The property name in the class implementing [IConfigCatalog] to hold the loaded configuration section.
 * @property kClass The settings [KClass] instantiate and assign to the [catalogProperty] from the [IConfigCatalog] instance.
 *
 * @see [IConfigCatalog]
 * @see [IConfigCatalogSection]
 */
@ConfigurationAPI
internal data class ConfigClassMap<T : IConfigCatalogSection>(
    val keyPath: String,
    val catalogProperty: String,
    val kClass: KClass<T>
)
