/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.domain.employment.graphql.kgraphql

import com.apurebase.kgraphql.Context
import com.apurebase.kgraphql.schema.dsl.SchemaBuilder
import kcrud.base.graphql.kgraphql.annotation.KGraphQLAPI
import kcrud.base.graphql.kgraphql.utils.GraphQLError
import kcrud.domain.employment.entities.EmploymentEntity
import kcrud.domain.employment.errors.EmploymentError
import kcrud.domain.employment.graphql.utils.EmploymentServiceResolver
import kcrud.domain.employment.service.EmploymentService
import java.util.*

/**
 * Employment query definitions.
 *
 * @param schemaBuilder The SchemaBuilder instance for configuring the schema.
 */
@KGraphQLAPI
class EmploymentQueries(private val schemaBuilder: SchemaBuilder) {

    /**
     * Configures query types specifically.
     */
    fun configureTypes(): EmploymentQueries {
        schemaBuilder.apply {
            type<EmploymentEntity> {
                description = "Query type definition for employments."
            }
        }

        return this
    }

    /**
     * Configures query resolvers to fetch data.
     */
    fun configureQueries(): EmploymentQueries {
        schemaBuilder.apply {
            query(name = "employment") {
                description = "Returns a single employment given its id."
                resolver { context: Context, employeeId: UUID, employmentId: UUID ->
                    val service: EmploymentService = EmploymentServiceResolver.get(context = context)
                    val employment: EmploymentEntity? = service.findById(
                        employeeId = employeeId,
                        employmentId = employmentId
                    )

                    employment
                        ?: GraphQLError.of(
                            error = EmploymentError.EmploymentNotFound(
                                employeeId = employeeId,
                                employmentId = employmentId
                            )
                        )
                }
            }
            query(name = "employments") {
                description = "Returns all employments for a given employee."
                resolver { context: Context, employeeId: UUID ->
                    val service: EmploymentService = EmploymentServiceResolver.get(context = context)
                    service.findByEmployeeId(employeeId = employeeId)
                }
            }
        }

        return this
    }
}
