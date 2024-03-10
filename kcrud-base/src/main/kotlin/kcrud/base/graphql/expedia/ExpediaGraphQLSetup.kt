/*
 * Copyright (c) 2023-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.graphql.expedia

import com.expediagroup.graphql.generator.SchemaGeneratorConfig
import com.expediagroup.graphql.generator.TopLevelObject
import com.expediagroup.graphql.generator.extensions.print
import com.expediagroup.graphql.generator.toSchema
import com.expediagroup.graphql.server.Schema
import com.expediagroup.graphql.server.ktor.*
import com.expediagroup.graphql.server.operations.Mutation
import com.expediagroup.graphql.server.operations.Query
import graphql.schema.GraphQLSchema
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import kcrud.base.graphql.expedia.annotation.ExpediaAPI
import kcrud.base.graphql.expedia.context.ContextFactory
import kcrud.base.graphql.expedia.types.CustomSchemaGeneratorHooks
import kcrud.base.infrastructure.utils.NetworkUtils
import kcrud.base.infrastructure.utils.Tracer
import kcrud.base.settings.AppSettings
import kcrud.base.settings.config.sections.GraphQLSettings
import java.io.File
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Sets up the GraphQL engine. Currently, using Expedia GraphQL.
 *
 * See: [Expedia GraphQL Documentation](https://opensource.expediagroup.com/graphql-kotlin/docs/)
 */
@ExpediaAPI
class ExpediaGraphQLSetup(
    private val application: Application,
    private val settings: GraphQLSettings
) {
    private val tracer = Tracer<ExpediaGraphQLSetup>()
    private val graphqlPackages: List<String> = listOf("kcrud")

    /**
     * Configures the GraphQL engine.
     *
     * @param queries The list of GraphQL queries to be configured.
     * @param mutations The list of GraphQL mutations to be configured.
     */
    fun configure(
        queries: List<Query>,
        mutations: List<Mutation>
    ) {
        tracer.info("Configuring ExpediaGroup GraphQL engine.")

        if (settings.playground) {
            tracer.byEnvironment("GraphQL playground is enabled.")
        }

        installGraphQL(
            schemaDirectives = KcrudSchema(),
            queriesSchema = queries,
            mutationsSchema = mutations
        )

        dumpSchema(queriesSchema = queries, mutationsSchema = mutations)
        setEndpoints()

        // Log GraphQL endpoints.
        if (!AppSettings.security.isEnabled) {
            NetworkUtils.logEndpoints(
                reason = "GraphQL",
                endpoints = listOf("graphiql", "sdl")
            )
        }
    }

    private fun installGraphQL(
        schemaDirectives: Schema,
        queriesSchema: List<Query>,
        mutationsSchema: List<Mutation>
    ) {
        application.install(GraphQL) {
            schema {
                packages = graphqlPackages
                queries = queriesSchema
                mutations = mutationsSchema
                schemaObject = schemaDirectives
                hooks = CustomSchemaGeneratorHooks()
            }

            server {
                contextFactory = ContextFactory()
            }
        }
    }

    private fun setEndpoints() {
        application.routing {
            authenticate(AppSettings.security.jwt.providerName, optional = !AppSettings.security.isEnabled) {
                graphQLGetRoute()
                graphQLPostRoute()
            }

            if (!AppSettings.security.isEnabled) {
                if (settings.playground) {
                    // Set GraphQL playground for development and testing.
                    // http://localhost:8080/graphiql
                    graphiQLRoute()
                }

                // SDL Schema.
                // http://localhost:8080/sdl
                graphQLSDLRoute()
            }
        }
    }

    private fun dumpSchema(queriesSchema: List<Query>, mutationsSchema: List<Mutation>) {
        if (!settings.dumpSchema || settings.schemaPath.isBlank())
            return

        tracer.byEnvironment("Dumping GraphQL schema.")

        val topLevelQueries: List<TopLevelObject> = queriesSchema.map { TopLevelObject(it) }
        val topLevelMutations: List<TopLevelObject> = mutationsSchema.map { TopLevelObject(it) }

        val schema: GraphQLSchema = toSchema(
            queries = topLevelQueries,
            mutations = topLevelMutations,
            config = SchemaGeneratorConfig(
                supportedPackages = graphqlPackages,
                hooks = CustomSchemaGeneratorHooks()
            )
        )

        val sdl: String = schema.print()
        val directoryPath: Path = Files.createDirectories(Paths.get(settings.schemaPath))
        val fileUri: URI = directoryPath.normalize().resolve("schema.graphql").toUri()
        val file = File(fileUri)
        file.writeText(text = sdl)

        tracer.info("Dumped GraphQL schema file:")
        tracer.info(file.absolutePath)
    }
}
