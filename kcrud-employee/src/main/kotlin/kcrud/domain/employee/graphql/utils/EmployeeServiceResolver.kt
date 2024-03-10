/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.domain.employee.graphql.utils

import com.apurebase.kgraphql.Context
import graphql.schema.DataFetchingEnvironment
import kcrud.base.graphql.expedia.annotation.ExpediaAPI
import kcrud.base.graphql.kgraphql.annotation.KGraphQLAPI
import kcrud.base.infrastructure.env.SessionContext
import kcrud.domain.contact.repository.ContactRepository
import kcrud.domain.contact.repository.IContactRepository
import kcrud.domain.employee.repository.EmployeeRepository
import kcrud.domain.employee.repository.IEmployeeRepository
import kcrud.domain.employee.service.EmployeeService
import kcrud.base.graphql.expedia.context.GraphQLEnv as ExpediaGraphQLEnv
import kcrud.base.graphql.kgraphql.context.GraphQLEnv as KGraphQLGraphQLEnv

internal object EmployeeServiceResolver {

    @OptIn(ExpediaAPI::class)
    fun get(env: DataFetchingEnvironment): EmployeeService {
        val sessionContext: SessionContext = ExpediaGraphQLEnv(env = env).sessionContext!!
        return resolve(sessionContext = sessionContext)
    }

    @OptIn(KGraphQLAPI::class)
    fun get(context: Context): EmployeeService {
        val sessionContext: SessionContext = KGraphQLGraphQLEnv(context = context).sessionContext!!
        return resolve(sessionContext = sessionContext)
    }

    private fun resolve(sessionContext: SessionContext): EmployeeService {
        val contactRepository: IContactRepository = ContactRepository(sessionContext = sessionContext)
        val employeeRepository: IEmployeeRepository = EmployeeRepository(
            sessionContext = sessionContext,
            contactRepository = contactRepository
        )
        return EmployeeService(sessionContext = sessionContext, employeeRepository = employeeRepository)
    }
}
