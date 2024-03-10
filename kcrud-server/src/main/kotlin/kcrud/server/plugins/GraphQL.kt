/*
 * Copyright (c) 2023-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.server.plugins

import io.ktor.server.application.*
import kcrud.base.graphql.GraphQLFramework
import kcrud.base.graphql.expedia.ExpediaGraphQLSetup
import kcrud.base.graphql.expedia.annotation.ExpediaAPI
import kcrud.base.graphql.kgraphql.KGraphQLSetup
import kcrud.base.graphql.kgraphql.annotation.KGraphQLAPI
import kcrud.base.graphql.kgraphql.types.SharedTypes
import kcrud.base.settings.AppSettings
import kcrud.domain.employee.graphql.expedia.EmployeeMutations as ExpediaEmployeeMutations
import kcrud.domain.employee.graphql.expedia.EmployeeQueries as ExpediaEmployeeQueries
import kcrud.domain.employee.graphql.kgraphql.EmployeeMutations as KGraphQLEmployeeMutations
import kcrud.domain.employee.graphql.kgraphql.EmployeeQueries as KGraphQLEmployeeQueries
import kcrud.domain.employee.graphql.kgraphql.EmployeeTypes as KGraphQLEmployeeTypes
import kcrud.domain.employment.graphql.expedia.EmploymentMutations as ExpediaEmploymentMutations
import kcrud.domain.employment.graphql.expedia.EmploymentQueries as ExpediaEmploymentQueries
import kcrud.domain.employment.graphql.kgraphql.EmploymentMutations as KGraphQLEmploymentMutations
import kcrud.domain.employment.graphql.kgraphql.EmploymentQueries as KGraphQLEmploymentQueries
import kcrud.domain.employment.graphql.kgraphql.EmploymentTypes as KGraphQLEmploymentTypes

/**
 * Sets up the GraphQL engine. Supported libraries are 'Expedia GraphQL' and 'KGraphQL'.
 *
 * See: [Expedia GraphQL Overview](https://opensource.expediagroup.com/graphql-kotlin/docs/server/ktor-server/ktor-overview)
 *
 * See: [Expedia GraphQL Documentation](https://opensource.expediagroup.com/graphql-kotlin/docs/)
 *
 * See: [Expedia GraphQL Repository](https://github.com/ExpediaGroup/graphql-kotlin)
 *
 * See: [KGraphQL Documentation](https://kgraphql.io/)
 *
 * See: [KGraphQL Repository](https://github.com/aPureBase/KGraphQL)
 */
fun Application.configureGraphQL() {

    if (!AppSettings.graphql.isEnabled) {
        return
    }

    when (AppSettings.graphql.framework) {
        GraphQLFramework.EXPEDIA_GROUP -> {
            configureExpedia(application = this)
        }

        GraphQLFramework.K_GRAPHQL -> {
            configureKGraphQL(application = this)
        }
    }
}

@OptIn(ExpediaAPI::class)
private fun configureExpedia(application: Application) {
    ExpediaGraphQLSetup(
        application = application,
        settings = AppSettings.graphql,
    ).configure(
        queries = listOf(
            ExpediaEmployeeQueries(),
            ExpediaEmploymentQueries()
        ),
        mutations = listOf(
            ExpediaEmployeeMutations(),
            ExpediaEmploymentMutations()
        )
    )
}

@OptIn(KGraphQLAPI::class)
private fun configureKGraphQL(application: Application) {
    KGraphQLSetup(
        application = application,
        settings = AppSettings.graphql,
    ).configure { schemaBuilder ->
        SharedTypes(schemaBuilder = schemaBuilder)
            .configure()

        KGraphQLEmployeeTypes(schemaBuilder = schemaBuilder)
            .configure()

        KGraphQLEmploymentTypes(schemaBuilder = schemaBuilder)
            .configure()

        KGraphQLEmployeeQueries(schemaBuilder = schemaBuilder)
            .configureInputs()
            .configureTypes()
            .configureQueries()

        KGraphQLEmployeeMutations(schemaBuilder = schemaBuilder)
            .configureInputs()
            .configureMutations()

        KGraphQLEmploymentQueries(schemaBuilder = schemaBuilder)
            .configureTypes()
            .configureQueries()

        KGraphQLEmploymentMutations(schemaBuilder = schemaBuilder)
            .configureInputs()
            .configureMutations()
    }
}
