/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.domain.employee.graphql.kgraphql

import com.apurebase.kgraphql.Context
import com.apurebase.kgraphql.schema.dsl.SchemaBuilder
import kcrud.base.graphql.kgraphql.annotation.KGraphQLAPI
import kcrud.base.graphql.kgraphql.utils.GraphQLError
import kcrud.domain.employee.entities.EmployeeEntity
import kcrud.domain.employee.entities.EmployeeRequest
import kcrud.domain.employee.errors.EmployeeError
import kcrud.domain.employee.graphql.utils.EmployeeServiceResolver
import kcrud.domain.employee.service.EmployeeService
import java.util.*

/**
 * Employee mutation definitions.
 *
 * @param schemaBuilder The SchemaBuilder instance for configuring the schema.
 */
@KGraphQLAPI
class EmployeeMutations(private val schemaBuilder: SchemaBuilder) {

    /**
     * Configures input types for mutations.
     */
    fun configureInputs(): EmployeeMutations {
        schemaBuilder.apply {
            inputType<EmployeeRequest> {
                name = "Input type definition for Employee."
            }
        }

        return this
    }

    /**
     * Configures mutation resolvers to modify data.
     */
    fun configureMutations(): EmployeeMutations {
        schemaBuilder.apply {
            mutation("createEmployee") {
                description = "Creates a new employee."
                resolver { context: Context, employee: EmployeeRequest ->
                    val service: EmployeeService = EmployeeServiceResolver.get(context = context)
                    service.create(employeeRequest = employee)
                }
            }

            mutation("updateEmployee") {
                description = "Updates an existing employee."
                resolver { context: Context, employeeId: UUID, employee: EmployeeRequest ->
                    val service: EmployeeService = EmployeeServiceResolver.get(context = context)
                    val updatedEmployee: EmployeeEntity? = service.update(employeeId = employeeId, employeeRequest = employee)
                    updatedEmployee
                        ?: GraphQLError.of(error = EmployeeError.EmployeeNotFound(employeeId = employeeId))
                }
            }

            mutation("deleteEmployee") {
                description = "Deletes an existing employee."
                resolver { context: Context, employeeId: UUID ->
                    val service: EmployeeService = EmployeeServiceResolver.get(context = context)
                    service.delete(employeeId = employeeId)
                }
            }

            mutation("deleteAllEmployees") {
                description = "Deletes all existing employees."
                resolver { context: Context ->
                    val service: EmployeeService = EmployeeServiceResolver.get(context = context)
                    service.deleteAll()
                }
            }
        }

        return this
    }
}
