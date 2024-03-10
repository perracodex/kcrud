/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.domain.employee.graphql.expedia

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Mutation
import graphql.execution.DataFetcherResult
import graphql.schema.DataFetchingEnvironment
import kcrud.base.graphql.expedia.annotation.ExpediaAPI
import kcrud.base.graphql.expedia.utils.GraphQLResult
import kcrud.domain.employee.entities.EmployeeEntity
import kcrud.domain.employee.entities.EmployeeRequest
import kcrud.domain.employee.errors.EmployeeError
import kcrud.domain.employee.graphql.utils.EmployeeServiceResolver
import kcrud.domain.employee.service.EmployeeService
import java.util.*

/**
 * Employee mutation definitions.
 */
@Suppress("unused")
@ExpediaAPI
class EmployeeMutations : Mutation {

    @GraphQLDescription("Creates a new employee.")
    suspend fun createEmployee(env: DataFetchingEnvironment, employee: EmployeeRequest): EmployeeEntity {
        val service: EmployeeService = EmployeeServiceResolver.get(env = env)
        return service.create(employeeRequest = employee)
    }

    @GraphQLDescription("Updates an existing employee.")
    suspend fun updateEmployee(
        env: DataFetchingEnvironment,
        employeeId: UUID,
        employee: EmployeeRequest
    ): DataFetcherResult<EmployeeEntity?> {
        val service: EmployeeService = EmployeeServiceResolver.get(env = env)
        val updatedEmployee: EmployeeEntity? = service.update(employeeId = employeeId, employeeRequest = employee)

        val error: EmployeeError.EmployeeNotFound? = if (updatedEmployee == null)
            EmployeeError.EmployeeNotFound(employeeId = employeeId)
        else
            null

        return GraphQLResult.of(data = updatedEmployee, error = error)
    }

    @GraphQLDescription("Deletes an existing employee.")
    suspend fun deleteEmployee(env: DataFetchingEnvironment, employeeId: UUID): Int {
        val service: EmployeeService = EmployeeServiceResolver.get(env = env)
        return service.delete(employeeId = employeeId)
    }

    @GraphQLDescription("Delete all employees.")
    suspend fun deleteAllEmployees(env: DataFetchingEnvironment): Int {
        val service: EmployeeService = EmployeeServiceResolver.get(env = env)
        return service.deleteAll()
    }
}
