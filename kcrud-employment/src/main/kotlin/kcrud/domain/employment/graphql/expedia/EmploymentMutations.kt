/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.domain.employment.graphql.expedia

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Mutation
import graphql.execution.DataFetcherResult
import graphql.schema.DataFetchingEnvironment
import kcrud.base.graphql.expedia.annotation.ExpediaAPI
import kcrud.base.graphql.expedia.utils.GraphQLResult
import kcrud.base.infrastructure.errors.KcrudException
import kcrud.domain.employment.entities.EmploymentEntity
import kcrud.domain.employment.entities.EmploymentRequest
import kcrud.domain.employment.errors.EmploymentError
import kcrud.domain.employment.graphql.utils.EmploymentServiceResolver
import kcrud.domain.employment.service.EmploymentService
import java.util.*

/**
 * Employment mutation definitions.
 */
@Suppress("unused")
@ExpediaAPI
class EmploymentMutations : Mutation {

    @GraphQLDescription("Creates a new employment.")
    suspend fun createEmployment(
        env: DataFetchingEnvironment,
        employeeId: UUID,
        employment: EmploymentRequest
    ): DataFetcherResult<EmploymentEntity?> {
        val service: EmploymentService = EmploymentServiceResolver.get(env = env)

        try {
            val newEmployment: EmploymentEntity = service.create(employeeId = employeeId, employmentRequest = employment)
            return GraphQLResult.of(data = newEmployment, error = null)
        } catch (e: KcrudException) {
            return GraphQLResult.of(data = null, error = e.error)
        }
    }

    @GraphQLDescription("Updates an existing employment.")
    suspend fun updateEmployment(
        env: DataFetchingEnvironment,
        employeeId: UUID,
        employmentId: UUID,
        employment: EmploymentRequest
    ): DataFetcherResult<EmploymentEntity?> {
        val service: EmploymentService = EmploymentServiceResolver.get(env = env)
        val updatedEmployment: EmploymentEntity?

        try {
            updatedEmployment = service.update(
                employeeId = employeeId,
                employmentId = employmentId,
                employmentRequest = employment
            )
        } catch (e: KcrudException) {
            return GraphQLResult.of(data = null, error = e.error)
        }

        val error = if (updatedEmployment == null)
            EmploymentError.EmploymentNotFound(employeeId = employeeId, employmentId = employmentId)
        else
            null

        return GraphQLResult.of(data = updatedEmployment, error = error)
    }

    @GraphQLDescription("Deletes an existing employment.")
    suspend fun deleteEmployment(env: DataFetchingEnvironment, employmentId: UUID): Int {
        val service: EmploymentService = EmploymentServiceResolver.get(env = env)
        return service.delete(employmentId = employmentId)
    }

    @GraphQLDescription("Deletes all employments for a given employee.")
    suspend fun deleteAllEmployments(env: DataFetchingEnvironment, employeeId: UUID): Int {
        val service: EmploymentService = EmploymentServiceResolver.get(env = env)
        return service.deleteAll(employeeId = employeeId)
    }
}
