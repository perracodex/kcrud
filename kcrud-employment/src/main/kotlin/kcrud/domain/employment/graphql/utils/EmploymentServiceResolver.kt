/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.domain.employment.graphql.utils

import com.apurebase.kgraphql.Context
import graphql.schema.DataFetchingEnvironment
import kcrud.base.graphql.expedia.annotation.ExpediaAPI
import kcrud.base.graphql.kgraphql.annotation.KGraphQLAPI
import kcrud.base.infrastructure.env.SessionContext
import kcrud.domain.employment.repository.EmploymentRepository
import kcrud.domain.employment.repository.IEmploymentRepository
import kcrud.domain.employment.service.EmploymentService
import kcrud.base.graphql.expedia.context.GraphQLEnv as ExpediaGraphQLEnv
import kcrud.base.graphql.kgraphql.context.GraphQLEnv as KGraphQLGraphQLEnv

internal object EmploymentServiceResolver {

    @OptIn(ExpediaAPI::class)
    fun get(env: DataFetchingEnvironment): EmploymentService {
        val sessionContext: SessionContext = ExpediaGraphQLEnv(env = env).sessionContext!!
        return resolve(sessionContext = sessionContext)
    }

    @OptIn(KGraphQLAPI::class)
    fun get(context: Context): EmploymentService {
        val sessionContext: SessionContext = KGraphQLGraphQLEnv(context = context).sessionContext!!
        return resolve(sessionContext = sessionContext)
    }

    private fun resolve(sessionContext: SessionContext): EmploymentService {
        val employmentRepository: IEmploymentRepository = EmploymentRepository(sessionContext = sessionContext)
        return EmploymentService(sessionContext = sessionContext, employmentRepository = employmentRepository)
    }
}
