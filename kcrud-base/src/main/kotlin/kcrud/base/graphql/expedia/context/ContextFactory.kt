/*
 * Copyright (c) 2023-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.graphql.expedia.context

import com.expediagroup.graphql.generator.extensions.toGraphQLContext
import com.expediagroup.graphql.server.ktor.DefaultKtorGraphQLContextFactory
import graphql.GraphQLContext
import io.ktor.server.auth.*
import io.ktor.server.request.*
import kcrud.base.graphql.expedia.annotation.ExpediaAPI
import kcrud.base.infrastructure.env.SessionContext

/**
 * Custom GraphQL context factory that adds a session context to the context.
 *
 * Then a query or mutation can access the session context by using the DataFetchingEnvironment.
 * See EmployeeQueries.kt for an example of how to access the session context.
 *
 * See: [Expedia GraphQL Documentation](https://opensource.expediagroup.com/graphql-kotlin/docs/schema-generator/execution/contextual-data/)
 */
@ExpediaAPI
class ContextFactory : DefaultKtorGraphQLContextFactory() {

    /**
     * Example demonstrating how to add a session actor from the request to the context.
     * More information can be added to the context if needed.
     */
    override suspend fun generateContext(request: ApplicationRequest): GraphQLContext {
        val sessionContext: SessionContext? = request.call.authentication.principal<SessionContext>()
        return mapOf(SessionContext::class to sessionContext).toGraphQLContext()
    }
}
