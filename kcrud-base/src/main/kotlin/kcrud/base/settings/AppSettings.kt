/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.settings

import io.ktor.server.config.*
import kcrud.base.env.Tracer
import kcrud.base.settings.annotation.ConfigurationAPI
import kcrud.base.settings.config.ConfigurationCatalog
import kcrud.base.settings.config.parser.ConfigClassMap
import kcrud.base.settings.config.parser.ConfigurationParser
import kcrud.base.settings.config.parser.IConfigSection
import kcrud.base.settings.config.sections.*
import kcrud.base.settings.config.sections.security.SecuritySettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.system.measureTimeMillis

/**
 * Singleton providing the configuration settings throughout the application.
 *
 * This class serves as the central point for accessing all configuration settings in a type-safe manner.
 */
object AppSettings {
    @Volatile
    private lateinit var configuration: ConfigurationCatalog

    val deployment: DeploymentSettings get() = configuration.deployment
    val runtime: RuntimeSettings get() = configuration.runtime
    val cors: CorsSettings get() = configuration.cors
    val database: DatabaseSettings get() = configuration.database
    val apiSchema: ApiSchemaSettings get() = configuration.apiSchema
    val security: SecuritySettings get() = configuration.security

    @OptIn(ConfigurationAPI::class)
    fun load(applicationConfig: ApplicationConfig) {
        if (AppSettings::configuration.isInitialized)
            return

        val tracer = Tracer.byFunction(ref = ::load)
        tracer.info("Loading application settings.")

        val timeTaken = measureTimeMillis {
            // Map connecting configuration paths.
            // Where the first argument is the path to the configuration section,
            // the second argument is the name of the constructor argument in the
            // ConfigurationCatalog class, and the third argument is the data class
            // that will be instantiated with the configuration values.
            val configMappings: List<ConfigClassMap<out IConfigSection>> = listOf(
                ConfigClassMap(path = "ktor.deployment", argument = "deployment", kClass = DeploymentSettings::class),
                ConfigClassMap(path = "runtime", argument = "runtime", kClass = RuntimeSettings::class),
                ConfigClassMap(path = "cors", argument = "cors", kClass = CorsSettings::class),
                ConfigClassMap(path = "database", argument = "database", kClass = DatabaseSettings::class),
                ConfigClassMap(path = "apiSchema", argument = "apiSchema", kClass = ApiSchemaSettings::class),
                ConfigClassMap(path = "security", argument = "security", kClass = SecuritySettings::class)
            )

            runBlocking {
                withContext(Dispatchers.IO) {
                    configuration = ConfigurationParser.parse(
                        configuration = applicationConfig,
                        mappings = configMappings
                    )
                }
            }
        }

        tracer.info("Application settings loaded. Time taken: $timeTaken ms.")
    }
}
