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
import kcrud.base.persistence.pagination.Page
import kcrud.base.persistence.pagination.Pageable
import kcrud.domain.employee.entities.EmployeeConnection
import kcrud.domain.employee.entities.EmployeeEntity
import kcrud.domain.employee.entities.EmployeeFilterSet
import kcrud.domain.employee.errors.EmployeeError
import kcrud.domain.employee.graphql.utils.EmployeeServiceResolver
import kcrud.domain.employee.service.EmployeeService
import java.util.*

/**
 * Employee query definitions.
 *
 * @param schemaBuilder The SchemaBuilder instance for configuring the schema.
 */
@KGraphQLAPI
class EmployeeQueries(private val schemaBuilder: SchemaBuilder) {

    /**
     * Configures input types for queries.
     */
    fun configureInputs(): EmployeeQueries {
        schemaBuilder.apply {
            inputType<EmployeeFilterSet> {
                name = "Input type definition for employee filters."
            }
            inputType<Pageable> {
                name = "Input type definition for pagination."
            }
        }

        return this
    }

    /**
     * Configures query types specifically.
     */
    fun configureTypes(): EmployeeQueries {
        schemaBuilder.apply {
            type<EmployeeEntity> {
                description = "Query type definition for employee."
            }
            type<EmployeeConnection> {
                description = "Query type definition for paginated employee query."
            }
        }

        return this
    }

    /**
     * Configures query resolvers to fetch data.
     */
    fun configureQueries(): EmployeeQueries {
        schemaBuilder.apply {
            query("employee") {
                description = "Returns a single employee given its id."
                resolver { context: Context, employeeId: UUID ->
                    val service: EmployeeService = EmployeeServiceResolver.get(context = context)
                    service.findById(employeeId = employeeId)
                        ?: GraphQLError.of(error = EmployeeError.EmployeeNotFound(employeeId = employeeId))
                }
            }

            query("employees") {
                description = "Returns all existing employees."
                resolver { context: Context, pageable: Pageable? ->
                    val service: EmployeeService = EmployeeServiceResolver.get(context = context)
                    val page: Page<EmployeeEntity> = service.findAll(pageable = pageable)
                    EmployeeConnection(page = page)
                }
            }

            query("filterEmployees") {
                description = "Filterable paginated Employee query."
                resolver { context: Context, filterSet: EmployeeFilterSet ->
                    val service: EmployeeService = EmployeeServiceResolver.get(context = context)
                    val page: Page<EmployeeEntity> = service.filter(filterSet = filterSet)
                    EmployeeConnection(page = page)
                }
            }
        }

        return this
    }
}
