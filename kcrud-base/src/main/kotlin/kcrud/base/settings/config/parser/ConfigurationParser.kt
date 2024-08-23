/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.settings.config.parser

import com.typesafe.config.ConfigException
import io.ktor.server.config.*
import kcrud.base.env.Tracer
import kcrud.base.settings.annotation.ConfigurationAPI
import kcrud.base.settings.config.ConfigurationCatalog
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.jvmErasure

/**
 * Automates the parsing of application configuration settings into Kotlin data classes.
 * Utilizes reflection for mapping configuration paths to data class types, supporting
 * both nested and simple structures in line with HOCON standards.
 *
 * Requirements for effective parsing:
 * - Data class properties must match configuration keys exactly.
 * - Configuration paths should mirror the data class hierarchy for proper mapping.
 * - The configuration file needs to be properly formatted according to HOCON specifications.
 *
 * Additionally, the parser handles list configurations as comma-separated strings or actual lists,
 * facilitating simpler configuration in environment variables. This allows settings defined as lists
 * in HOCON to be represented as comma-delimited strings in environmental variables.
 */
@ConfigurationAPI
internal object ConfigurationParser {
    private val tracer = Tracer<ConfigurationParser>()

    /**
     * Represents the delimiter used for separating elements in a list,
     * when the list is represented as a single string in the configuration.
     */
    private const val ARRAY_DELIMITER: Char = ';'

    /**
     * Represents a mapping from a constructor parameter to its corresponding configuration value.
     *
     * @property parameter The target constructor [KParameter].
     * @property value The value corresponding to the constructor [parameter].
     */
    private data class ParameterMapping(val parameter: KParameter, var value: Any)

    /**
     * Performs the application configuration parsing.
     * Top-level configurations are parsed concurrently.
     *
     * @param configuration The application configuration object to be parsed.
     * @param configMappings Map of top-level configuration paths to their corresponding classes.
     * @return A new [ConfigurationCatalog] object populated with the parsed configuration data.
     */
    suspend fun parse(
        configuration: ApplicationConfig,
        configMappings: List<ConfigClassMap<out IConfigSection>>
    ): ConfigurationCatalog {

        // Retrieve the primary constructor of the ConfigurationCatalog class,
        // which will be used to instantiate the output object.
        val configConstructor: KFunction<ConfigurationCatalog> = ConfigurationCatalog::class.primaryConstructor
            ?: throw IllegalArgumentException(
                "Primary constructor is required for ${ConfigurationCatalog::class.simpleName}."
            )

        // Map each configuration path to its corresponding class,
        // and construct the arguments map for the output object.
        val constructorArguments: Map<KParameter, Any?> = withContext(Dispatchers.IO) {
            val tasks: List<Deferred<ParameterMapping>> = configMappings.map { configClassMap ->
                async {
                    // Map each configuration path to its corresponding class.
                    // Nested settings are handled recursively.
                    val configInstance: Any = instantiateConfig(
                        config = configuration,
                        keyPath = configClassMap.path,
                        kClass = configClassMap.kClass
                    )

                    // Find the constructor parameter corresponding to the configuration class.
                    val parameter: KParameter = configConstructor.parameters.find { parameter ->
                        parameter.name == configClassMap.mappingName
                    } ?: throw IllegalArgumentException("Config argument for ${configClassMap.mappingName} not found.")

                    // Return the mapping of the constructor argument parameter to its value.
                    ParameterMapping(parameter = parameter, value = configInstance)
                }
            }

            // Await all results and construct the arguments map.
            tasks.map { mapping -> mapping.await() }.associate { mapping ->
                mapping.parameter to mapping.value
            }
        }

        // Create the instance of the ConfigurationCatalog class with the parsed configuration values.
        return configConstructor.callBy(args = constructorArguments)
    }

    /**
     * Dynamically instantiates an object of the specified KClass using the primary constructor.
     * Supports both simple and nested data class types. For each constructor parameter,
     * fetches the corresponding configuration value from the specified key path.
     *
     * Data classes constructor parameters and setting key names must match exactly.
     *
     * @param config The application configuration object.
     * @param keyPath The base path in the configuration for fetching values.
     * @param kClass The KClass of the type to instantiate.
     * @return An instance of the specified class with properties populated from the configuration.
     * @throws IllegalArgumentException If a required configuration key is missing or if there is a type mismatch.
     */
    private fun <T : Any> instantiateConfig(config: ApplicationConfig, keyPath: String, kClass: KClass<T>): T {
        tracer.debug("Parsing '${kClass.simpleName}' from '$keyPath'")

        // Fetch the primary constructor of the class.
        val constructor: KFunction<T> = kClass.primaryConstructor!!

        // Map each constructor parameter to its corresponding value from the configuration.
        // This includes direct value assignment for simple types and recursive instantiation
        // for nested data classes.
        val arguments: Map<KParameter, Any?> = constructor.parameters.associateWith { parameter ->
            val parameterType: KClass<*> = parameter.type.jvmErasure
            val parameterKeyPath = "$keyPath.${parameter.name}"

            if (parameterType.isData) {
                // Recursive instantiation for nested data classes.
                instantiateConfig(
                    config = config,
                    keyPath = parameterKeyPath,
                    kClass = parameterType
                )
            } else {
                // Find the target property attribute corresponding to the parameter in the class.
                val property: KProperty1<T, *> = kClass.memberProperties.find {
                    it.name == parameter.name
                }!!

                // Convert and return the configuration value for the parameter.
                convertToType(
                    config = config,
                    keyPath = parameterKeyPath,
                    type = parameterType,
                    property = property
                )
            }
        }

        // Create an instance of the class with the obtained configuration values.
        return runCatching {
            constructor.callBy(args = arguments)
        }.getOrElse { error ->
            val errorMessage: String = error.message ?: error.cause?.toString() ?: "Unknown error"
            throw IllegalArgumentException(
                "Error instantiating class $kClass at '$keyPath':\n$errorMessage\nArguments: $arguments"
            )
        }
    }

    /**
     * Converts a configuration property to the given type.
     *
     * Retrieves a property from the configuration based on the keyPath and converts it
     * to the specified type. For data classes, it recursively instantiates them.
     *
     * @param config The application configuration object.
     * @param keyPath The key path for the property in the configuration.
     * @param type The KClass to which the property should be converted.
     * @param property The property attribute from the type's KClass.
     * @return The converted property value or null if not found.
     * @throws IllegalArgumentException for unsupported types or conversion failures.
     */
    private fun convertToType(
        config: ApplicationConfig,
        keyPath: String,
        type: KClass<*>,
        property: KProperty1<*, *>
    ): Any? {
        // Handle data classes. Recursively instantiate them.
        if (type.isData) {
            return instantiateConfig(config = config, keyPath = keyPath, kClass = type)
        }

        // Handle lists.
        if (type == List::class) {
            val listType: KClass<*> = (property.returnType.arguments.first().type!!.classifier as? KClass<*>)!!
            return parseListValues(config = config, keyPath = keyPath, listType = listType)
        }

        // Handle simple types.
        val stringValue: String = config.tryGetString(key = keyPath) ?: return null
        return parseElementValue(keyPath = keyPath, stringValue = stringValue, type = type)
    }

    /**
     * Function to parse an element into its final value.
     *
     * @param keyPath The key path for the property in the configuration.
     * @param stringValue The string value to convert.
     * @param type The KClass to which the property should be converted.
     * @return The converted property value or null if not found.
     * @throws IllegalArgumentException For unsupported types or conversion failures.
     */
    private fun parseElementValue(keyPath: String, stringValue: String, type: KClass<*>): Any? {
        val key = "$keyPath: $stringValue"

        return when {
            type == String::class -> stringValue

            type == Boolean::class -> stringValue.toBooleanStrictOrNull()
                ?: throw IllegalArgumentException("Invalid Boolean value in: '$key'")

            type == Int::class -> stringValue.toIntOrNull()
                ?: throw IllegalArgumentException("Invalid Int value in: '$key'")

            type == Long::class -> stringValue.toLongOrNull()
                ?: throw IllegalArgumentException("Invalid Long value in: '$key'")

            type == Double::class -> stringValue.toDoubleOrNull()
                ?: throw IllegalArgumentException("Invalid Double value in: '$key'")

            type.java.isEnum -> {
                // Check if the config value is build by single string with comma-delimited values.
                if (stringValue.contains(char = ARRAY_DELIMITER)) {
                    // Split the string by commas and trim spaces, then convert each part to enum.
                    stringValue.split(ARRAY_DELIMITER).mapNotNull { part ->
                        convertToEnum(enumType = type, stringValue = part.trim(), keyPath = keyPath)
                    }
                } else {
                    // If there is only one single value, without commas, convert it directly to enum.
                    convertToEnum(enumType = type, stringValue = stringValue, keyPath = keyPath)
                }
            }

            else -> throw IllegalArgumentException("Unsupported type '$type' in '$key'")
        }
    }

    /**
     * Converts a string value to an enum.
     *
     * @param enumType The enum type to which the string value should be converted.
     * @param stringValue The string value to convert.
     * @param keyPath The key path for the property in the configuration.
     * @return The converted enum value or null if not found.
     * @throws IllegalArgumentException If the enum value is not found.
     */
    private fun convertToEnum(enumType: KClass<*>, stringValue: String, keyPath: String): Enum<*>? {
        if (stringValue.isBlank() || stringValue.lowercase() == "null") {
            return null
        }

        return enumType.java.enumConstants.firstOrNull {
            (it as Enum<*>).name.compareTo(stringValue, ignoreCase = true) == 0
        } as Enum<*>?
            ?: throw IllegalArgumentException(
                "Enum value '$stringValue' not found for type: $enumType. Found in path: $keyPath"
            )
    }

    /**
     * Parses a list from the configuration.
     * Lists can be specified as a single string, comma-separated, or as a list of strings.
     * The list is mapped to the specified type.
     *
     * @param config The application configuration object.
     * @param keyPath The key path for the property in the configuration.
     * @param listType The KClass to which the list elements should be converted.
     * @return The converted list or an empty list if not found.
     */
    private fun parseListValues(
        config: ApplicationConfig,
        keyPath: String,
        listType: KClass<*>
    ): List<Any?> {
        val rawList: List<String> = try {
            // Attempt to retrieve it as a list.
            config.tryGetStringList(key = keyPath) ?: listOf()
        } catch (e: ConfigException) {
            // If failed to get a list, then treat it as a single string with comma-delimited values.
            val stringValue: String = config.tryGetString(key = keyPath) ?: ""

            if (stringValue.contains(char = ARRAY_DELIMITER)) {
                stringValue.split(ARRAY_DELIMITER).map { it.trim() }
            } else {
                listOf(stringValue.trim())
            }
        }

        // Map each element of the list to its respective type.
        return rawList.map { listElementValue ->
            parseElementValue(keyPath = keyPath, stringValue = listElementValue, type = listType)
        }
    }
}
