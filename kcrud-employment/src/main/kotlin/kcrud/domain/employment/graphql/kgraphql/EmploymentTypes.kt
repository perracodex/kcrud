/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.domain.employment.graphql.kgraphql

import com.apurebase.kgraphql.schema.dsl.SchemaBuilder
import kcrud.base.database.schema.employment.types.WorkModality
import kcrud.base.graphql.kgraphql.annotation.KGraphQLAPI

/**
 * Concrete employment types for the KGraphQL API.
 *
 * @param schemaBuilder The SchemaBuilder instance for configuring the schema.
 */
@KGraphQLAPI
class EmploymentTypes(private val schemaBuilder: SchemaBuilder) {

    fun configure(): EmploymentTypes {
        schemaBuilder.apply {
            enum<WorkModality> {
                description = "The employment's work modality."
            }
        }

        return this
    }
}
