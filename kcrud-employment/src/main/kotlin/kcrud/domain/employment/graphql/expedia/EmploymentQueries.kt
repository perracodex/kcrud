/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.domain.employment.graphql.expedia

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Query
import graphql.execution.DataFetcherResult
import graphql.schema.DataFetchingEnvironment
import kcrud.base.graphql.expedia.annotation.ExpediaAPI
import kcrud.base.graphql.expedia.utils.GraphQLResult
import kcrud.domain.employment.entities.EmploymentEntity
import kcrud.domain.employment.errors.EmploymentError
import kcrud.domain.employment.graphql.utils.EmploymentServiceResolver
import kcrud.domain.employment.service.EmploymentService
import java.util.*

/**
 * Employment query definitions.
 */
@ExpediaAPI
class EmploymentQueries : Query {

    @GraphQLDescription("Returns a concrete employment for a given employee.")
    suspend fun employment(
        env: DataFetchingEnvironment,
        employeeId: UUID,
        employmentId: UUID
    ): DataFetcherResult<EmploymentEntity?> {
        val service: EmploymentService = EmploymentServiceResolver.get(env = env)
        val employment: EmploymentEntity? = service.findById(employeeId = employeeId, employmentId = employmentId)

        val error = if (employment == null)
            EmploymentError.EmploymentNotFound(employeeId = employeeId, employmentId = employmentId)
        else
            null

        return GraphQLResult.of(data = employment, error = error)
    }

    @GraphQLDescription("Returns all employments for a given employee.")
    suspend fun employments(env: DataFetchingEnvironment, employeeId: UUID): List<EmploymentEntity> {
        val service: EmploymentService = EmploymentServiceResolver.get(env = env)
        return service.findByEmployeeId(employeeId = employeeId)
    }
}
