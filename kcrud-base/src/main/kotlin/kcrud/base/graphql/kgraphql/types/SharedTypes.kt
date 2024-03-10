/*
 * Copyright (c) 2023-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.graphql.kgraphql.types

import com.apurebase.kgraphql.schema.dsl.SchemaBuilder
import kcrud.base.graphql.kgraphql.annotation.KGraphQLAPI
import kcrud.base.infrastructure.utils.KLocalDate
import kcrud.base.infrastructure.utils.KLocalDateTime
import kcrud.base.persistence.pagination.Pageable
import kcrud.base.persistence.utils.toUUID
import java.time.DayOfWeek
import java.time.Month
import java.util.*

/**
 * Concrete shared types for the KGraphQL API.
 *
 * @param schemaBuilder The SchemaBuilder instance for configuring the schema.
 */
@KGraphQLAPI
class SharedTypes(private val schemaBuilder: SchemaBuilder) {

    /**
     * Configures common types like enums and scalars that are used in both queries and mutations.
     */
    fun configure(): SharedTypes {
        schemaBuilder.apply {
            enum<DayOfWeek> {
                description = "Day of week."
            }
            enum<Month> {
                description = "Month in a year."
            }
            stringScalar<KLocalDate> {
                serialize = { date -> date.toString() }
                deserialize = { str -> KLocalDate.parse(str) }
            }
            stringScalar<KLocalDateTime> {
                serialize = { datetime -> datetime.toString() }
                deserialize = { str -> KLocalDateTime.parse(str) }
            }
            stringScalar<UUID> {
                description = "UUID unique identifier"
                serialize = { uuid -> uuid.toString() }
                deserialize = { str -> str.toUUID() }
            }
            enum<Pageable.Direction> {
                description = "Sort direction used in pagination."
            }
        }

        return this
    }
}
