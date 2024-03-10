/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.domain.employee.graphql.expedia

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Query
import graphql.execution.DataFetcherResult
import graphql.schema.DataFetchingEnvironment
import kcrud.base.graphql.expedia.annotation.ExpediaAPI
import kcrud.base.graphql.expedia.utils.GraphQLResult
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
 * These queries include also examples of how to add descriptions to the schema
 * for both the query itself and its parameters.
 */
@Suppress("unused")
@ExpediaAPI
class EmployeeQueries : Query {

    @GraphQLDescription("Returns an employee given its id.")
    suspend fun employee(
        env: DataFetchingEnvironment,
        @GraphQLDescription("The target employee to be returned.")
        employeeId: UUID
    ): DataFetcherResult<EmployeeEntity?> {
        val service: EmployeeService = EmployeeServiceResolver.get(env = env)
        val employee: EmployeeEntity? = service.findById(employeeId = employeeId)

        val error: EmployeeError.EmployeeNotFound? = if (employee == null)
            EmployeeError.EmployeeNotFound(employeeId = employeeId)
        else
            null

        return GraphQLResult.of(data = employee, error = error)
    }

    @GraphQLDescription("Returns all existing employees.")
    suspend fun employees(
        env: DataFetchingEnvironment,
        @GraphQLDescription("Pagination options. If not provided, a single page is returned.")
        pageable: Pageable? = null
    ): EmployeeConnection {
        val service: EmployeeService = EmployeeServiceResolver.get(env = env)
        val page: Page<EmployeeEntity> = service.findAll(pageable = pageable)
        return EmployeeConnection(page = page)
    }

    @GraphQLDescription("Filterable paginated Employee query.")
    suspend fun filterEmployees(
        env: DataFetchingEnvironment,
        @GraphQLDescription("Filter set options to be applied.")
        filterSet: EmployeeFilterSet
    ): EmployeeConnection {
        val service: EmployeeService = EmployeeServiceResolver.get(env = env)
        val page: Page<EmployeeEntity> = service.filter(filterSet = filterSet)
        return EmployeeConnection(page = page)
    }
}
