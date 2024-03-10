/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.infrastructure.health.checks

import kcrud.base.graphql.GraphQLFramework
import kcrud.base.infrastructure.env.EnvironmentType
import kcrud.base.infrastructure.health.annotation.HealthCheckAPI
import kcrud.base.settings.AppSettings
import kotlinx.serialization.Serializable

@HealthCheckAPI
@Serializable
data class GraphQLCheck(
    val errors: MutableList<String>,
    val enabled: Boolean,
    val framework: GraphQLFramework,
    val playground: Boolean,
    val dumpSchema: Boolean,
    val schemaPath: String
) {
    constructor() : this(
        errors = mutableListOf(),
        enabled = AppSettings.graphql.isEnabled,
        framework = AppSettings.graphql.framework,
        playground = AppSettings.graphql.playground,
        dumpSchema = AppSettings.graphql.dumpSchema,
        schemaPath = AppSettings.graphql.schemaPath
    )

    init {
        val className: String? = this::class.simpleName
        val environment: EnvironmentType = AppSettings.runtime.environment

        if (environment == EnvironmentType.PROD) {
            if (playground) {
                errors.add(
                    "$className. GraphQL Playground is enabled in '$environment' environment. " +
                            "'$framework' framework. Enabled: $enabled."
                )
            }

            if (dumpSchema) {
                errors.add(
                    "$className. GraphQL Schema Dump is enabled in '$environment' environment. " +
                            "'$framework' framework. Enabled: $enabled."
                )
            }

            if (dumpSchema && schemaPath.isBlank()) {
                errors.add(
                    "$className. GraphQL Schema Dump is enabled but no schema path is provided. " +
                            "'$framework' framework. Enabled: $enabled."
                )
            }
        }
    }
}
