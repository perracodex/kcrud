/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
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
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.system.measureTimeMillis

/**
 * Singleton providing the configuration settings throughout the application.
 *
 * This class serves as the central point for accessing all configuration settings in a type-safe manner.
 */
public object AppSettings {
    @Volatile
    private lateinit var configuration: ConfigurationCatalog

    /** The API schema settings. */
    public val apiSchema: ApiSchemaSettings get() = configuration.apiSchema

    /** The CORS settings. */
    public val cors: CorsSettings get() = configuration.cors

    /** The database settings. */
    public val database: DatabaseSettings get() = configuration.database

    /** The deployment settings. */
    public val deployment: DeploymentSettings get() = configuration.deployment

    /** The runtime settings. */
    public val runtime: RuntimeSettings get() = configuration.runtime

    /** The application security settings. */
    public val security: SecuritySettings get() = configuration.security

    /**
     * Loads the application settings from the provided [ApplicationConfig].
     * Will only load the settings once.
     *
     * @param applicationConfig The [ApplicationConfig] to load the settings from.
     */
    @OptIn(ConfigurationAPI::class)
    public fun load(applicationConfig: ApplicationConfig) {
        if (AppSettings::configuration.isInitialized) {
            return
        }

        val tracer = Tracer(ref = ::load)
        tracer.info("Loading application settings.")

        val timeTaken = measureTimeMillis {
            // Map connecting configuration paths.
            // Where the first argument is the path to the configuration section,
            // the second argument is the name of the constructor argument in the
            // ConfigurationCatalog class, and the third argument is the data class
            // that will be instantiated with the configuration values.
            val configMappings: List<ConfigClassMap<out IConfigSection>> = listOf(
                ConfigClassMap(mappingName = "apiSchema", path = "apiSchema", kClass = ApiSchemaSettings::class),
                ConfigClassMap(mappingName = "cors", path = "cors", kClass = CorsSettings::class),
                ConfigClassMap(mappingName = "database", path = "database", kClass = DatabaseSettings::class),
                ConfigClassMap(mappingName = "deployment", path = "ktor.deployment", kClass = DeploymentSettings::class),
                ConfigClassMap(mappingName = "runtime", path = "runtime", kClass = RuntimeSettings::class),
                ConfigClassMap(mappingName = "security", path = "security", kClass = SecuritySettings::class)
            )

            runBlocking {
                configuration = ConfigurationParser.parse(
                    configuration = applicationConfig,
                    configMappings = configMappings
                )
            }
        }

        tracer.info("Application settings loaded. Time taken: $timeTaken ms.")
    }

    /**
     * Serializes the current settings configuration to a JSON string.
     * The [load] method must be called before this method.
     *
     * @return The JSON string representation of the current settings.
     */
    public fun serialize(): String {
        return Json.encodeToString<ConfigurationCatalog>(value = configuration)
    }

    /**
     * Deserializes the provided JSON string into the current settings.
     *
     * @param string The JSON string to deserialize.
     */
    public fun deserialize(string: String) {
        configuration = Json.decodeFromString<ConfigurationCatalog>(string = string)
    }
}
