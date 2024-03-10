/*
 * Copyright (c) 2023-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.graphql.kgraphql

import com.apurebase.kgraphql.GraphQL
import com.apurebase.kgraphql.schema.dsl.SchemaBuilder
import io.ktor.server.application.*
import io.ktor.server.auth.*
import kcrud.base.infrastructure.env.SessionContext
import kcrud.base.infrastructure.utils.NetworkUtils
import kcrud.base.infrastructure.utils.Tracer
import kcrud.base.settings.AppSettings
import kcrud.base.settings.config.sections.GraphQLSettings

/**
 * Sets up the GraphQL engine. Currently, using KGraphQL.
 *
 * See: [KGraphQL Documentation](https://kgraphql.io/)
 */
class KGraphQLSetup(
    private val application: Application,
    private val settings: GraphQLSettings,
) {
    private val tracer = Tracer<KGraphQLSetup>()

    /**
     * Configures the GraphQL engine.
     *
     * @param configureSchema The lambda function to configure the GraphQL schema.
     */
    fun configure(configureSchema: (SchemaBuilder) -> Unit) {
        tracer.info("Configuring KGraphQL engine.")

        if (settings.playground) {
            tracer.byEnvironment("GraphQL playground is enabled.")
        }

        application.install(GraphQL) {

            // Wrap the route with authentication.
            wrap {
                authenticate(
                    AppSettings.security.jwt.providerName,
                    optional = !AppSettings.security.isEnabled,
                    build = it
                )
            }

            // Set GraphQL playground for development and testing.
            playground = settings.playground

            // Set the security context to verify the JWT token for each incoming GraphQL request.
            context { call ->
                call.authentication.principal<SessionContext>()?.let { sessionContext ->
                    // If validation passed then add the actor to the context,
                    // so it can be used in the resolvers.
                    +sessionContext
                }
            }

            // Define the GraphQL schema.
            schema {
                configureSchema(this)
            }
        }

        if (settings.playground) {
            NetworkUtils.logEndpoints(
                reason = "GraphQL",
                endpoints = listOf("graphql")
            )
        }
    }
}
