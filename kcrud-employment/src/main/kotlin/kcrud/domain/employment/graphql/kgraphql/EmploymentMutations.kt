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
import kcrud.domain.employment.entities.EmploymentRequest
import kcrud.domain.employment.errors.EmploymentError
import kcrud.domain.employment.graphql.utils.EmploymentServiceResolver
import kcrud.domain.employment.service.EmploymentService
import java.util.*

/**
 * Employment mutation definitions.
 *
 * @param schemaBuilder The SchemaBuilder instance for configuring the schema.
 */
@KGraphQLAPI
class EmploymentMutations(private val schemaBuilder: SchemaBuilder) {

    /**
     * Configures input types for mutations.
     */
    fun configureInputs(): EmploymentMutations {
        schemaBuilder.apply {
            inputType<EmploymentRequest> {
                name = "Input type definition for Employments."
            }
        }

        return this
    }

    fun configureMutations(): EmploymentMutations {
        schemaBuilder.apply {
            mutation(name = "createEmployment") {
                description = "Creates a new employment."
                resolver { context: Context, employeeId: UUID, employment: EmploymentRequest ->
                    val service: EmploymentService = EmploymentServiceResolver.get(context = context)
                    service.create(employeeId = employeeId, employmentRequest = employment)
                }
            }

            mutation(name = "updateEmployment") {
                description = "Updates an existing employment."
                resolver { context: Context, employeeId: UUID, employmentId: UUID, employment: EmploymentRequest ->
                    val service: EmploymentService = EmploymentServiceResolver.get(context = context)

                    val updatedEmployment: EmploymentEntity? = service.update(
                        employeeId = employeeId,
                        employmentId = employmentId,
                        employmentRequest = employment
                    )

                    updatedEmployment
                        ?: GraphQLError.of(
                            error = EmploymentError.EmploymentNotFound(
                                employeeId = employeeId,
                                employmentId = employmentId
                            )
                        )
                }
            }

            mutation(name = "deleteEmployment") {
                description = "Deletes an existing employment."
                resolver { context: Context, employmentId: UUID ->
                    val service: EmploymentService = EmploymentServiceResolver.get(context = context)
                    service.delete(employmentId = employmentId)
                }
            }

            mutation(name = "deleteAllEmployments") {
                description = "Deletes all employments for an existing employee."
                resolver { context: Context, employeeId: UUID ->
                    val service: EmploymentService = EmploymentServiceResolver.get(context = context)
                    service.deleteAll(employeeId = employeeId)
                }
            }
        }

        return this
    }
}
