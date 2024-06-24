/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.access.rbac.entity.base

import kcrud.access.rbac.service.RbacFieldAnonymization
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

/**
 * Base class for entities that should support field level anonymization.
 *
 * It provides functionality to anonymize specified fields of an entity using reflection,
 * creating a new instance of the entity with certain fields replaced by predefined placeholders.
 *
 * While this class defines the core functionality for anonymization, it still requires concrete
 * implementations to utilize the [anonymize] method to specify which fields should be anonymized.
 *
 * Using placeholders instead of enforcing the target entity to use null arguments, ensures that
 * the anonymized entity remains consistent with its original structure, which is important for
 * serialization and maintains immutability.
 *
 * This class has scope for improvement, such as handling nested entities, and caching the reflection
 * results for performance optimization.
 *
 * @see [RbacFieldAnonymization]
 */
abstract class BaseRbacEntity {

    /**
     * Anonymizes specified fields by using reflection to create a new instance
     * of the entity with the fields anonymized.
     *
     * Anonymized fields are replaced with predefined placeholders based on their type.
     * Nested entity fields are also supported.
     *
     * Usage example:
     * ```
     * data class SomeEntity(val name: String, val phone: String): RbacEntity()
     * val entity = SomeEntity(...)
     * val anonymizedEntity = entity.anonymize(listOf("phone"))
     * ```
     *
     * @param T The type of the entity, automatically inferred from the inheriting class.
     * @param fields A list of strings indicating which fields of the entity should be anonymized.
     *               The fields are case-insensitive and should match the exact name of the property.
     *               If null or empty, the original entity is returned without changes.
     * @return A new instance of the entity with specified fields anonymized.
     */
    inline fun <reified T : BaseRbacEntity> anonymize(fields: List<String>?): T {
        // Delegate to the internal, non-inline anonymization method while preserving the type information.
        return internalAnonymize(fields, T::class) as T
    }

    /**
     * Handles the actual anonymization logic, capable of handling nested entities. This method is not inline,
     * enabling it to be recursively called without causing issues related to inline recursion.
     *
     * @param fields A list of strings representing the fields to be anonymized, supporting nested notation.
     * @param clazz The KClass instance of the entity type, used for reflection.
     * @return A new instance of the entity (or nested entity) with specified fields anonymized.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : BaseRbacEntity> internalAnonymize(fields: List<String>?, clazz: KClass<T>): BaseRbacEntity {
        if (fields.isNullOrEmpty()) {
            // If no fields are specified for anonymization, return the entity as is.
            return this
        }

        // Separate the fields into top-level fields and nested fields based on dot notation.
        val topLevelFields: List<String> = fields.filter {
            !it.contains(char = '.')
        }
        val nestedFields: Map<String, List<String>> = fields.filter {
            it.contains(char = '.')
        }.groupBy {
            it.substringBefore(delimiter = '.')
        }

        // Retrieve the primary constructor of the entity class.
        val constructor: KFunction<T> = clazz.primaryConstructor!!

        // Create a map of constructor parameters and their corresponding anonymized values where applicable.
        val arguments: Map<KParameter, Any?> = constructor.parameters.associateWith { parameter ->
            // Find the corresponding property for each constructor parameter.
            val property: KProperty1<T, *>? = clazz.memberProperties.firstOrNull { it.name == parameter.name }

            if (property != null) {
                if (property.name in topLevelFields) {
                    // Anonymize top-level fields directly.
                    return@associateWith RbacFieldAnonymization.anonymize(value = property.get(this as T))
                } else {
                    // Handle nested fields: find the nested entity and recursively anonymize it.
                    val nestedPropertyName = property.name

                    if (nestedPropertyName in nestedFields.keys) {
                        val nestedEntity: BaseRbacEntity? = property.get(this as T) as? BaseRbacEntity
                        val newFields: List<String>? = nestedFields[nestedPropertyName]?.map { it.substringAfter(delimiter = '.') }
                        return@associateWith nestedEntity?.internalAnonymize(newFields, nestedEntity::class) ?: property.get(this)
                    } else {
                        // If not a nested field or no anonymization needed, keep the original value.
                        return@associateWith property.get(this as T)
                    }
                }
            } else {
                // If the property does not exist, return null (should not happen with well-formed data).
                return@associateWith null
            }
        }

        // Construct and return the new instance of the entity with anonymized fields.
        return constructor.callBy(args = arguments)
    }
}
