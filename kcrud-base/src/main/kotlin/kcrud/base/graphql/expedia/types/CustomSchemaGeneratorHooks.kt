/*
 * Copyright (c) 2023-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.graphql.expedia.types

import com.expediagroup.graphql.generator.hooks.SchemaGeneratorHooks
import graphql.schema.GraphQLType
import kcrud.base.infrastructure.utils.KLocalDate
import kcrud.base.infrastructure.utils.KLocalDateTime
import java.util.*
import kotlin.reflect.KType

/**
 * Generate custom GraphQL types which are not supported by default.
 */
class CustomSchemaGeneratorHooks : SchemaGeneratorHooks {
    override fun willGenerateGraphQLType(type: KType): GraphQLType? {
        return when (type.classifier) {
            UUID::class -> GraphUUIDType
            KLocalDate::class -> GraphLocalDateType
            KLocalDateTime::class -> GraphLocalDateTimeType
            else -> super.willGenerateGraphQLType(type)
        }
    }
}
