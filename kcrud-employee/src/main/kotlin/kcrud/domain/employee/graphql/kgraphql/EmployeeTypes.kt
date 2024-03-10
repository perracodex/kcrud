/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.domain.employee.graphql.kgraphql

import com.apurebase.kgraphql.schema.dsl.SchemaBuilder
import kcrud.base.database.schema.employee.types.Honorific
import kcrud.base.database.schema.employee.types.MaritalStatus
import kcrud.base.graphql.kgraphql.annotation.KGraphQLAPI

/**
 * Concrete employee types for the KGraphQL API.
 *
 * @param schemaBuilder The SchemaBuilder instance for configuring the schema.
 */
@KGraphQLAPI
class EmployeeTypes(private val schemaBuilder: SchemaBuilder) {

    /**
     * Configures common types like enums and scalars that are used in both queries and mutations.
     */
    fun configure(): EmployeeTypes {
        schemaBuilder.apply {
            enum<MaritalStatus> {
                description = "The employee's marital status."
            }
            enum<Honorific> {
                description = "The employee's honorific title."
            }
        }

        return this
    }
}
