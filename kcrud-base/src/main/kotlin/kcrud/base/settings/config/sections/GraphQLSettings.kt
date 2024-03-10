/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.settings.config.sections

import kcrud.base.graphql.GraphQLFramework
import kcrud.base.settings.config.parser.IConfigSection

/**
 * GraphQL related settings.
 *
 * @property isEnabled Whether GraphQL is enabled.
 * @property framework The GraphQL framework to use.
 * @property playground Whether to enable the GraphQL Playground.
 * @property dumpSchema Whether to dump the GraphQL schema.
 * @property schemaPath The path to the GraphQL schema file.
 */
data class GraphQLSettings(
    val isEnabled: Boolean,
    val framework: GraphQLFramework,
    val playground: Boolean,
    val dumpSchema: Boolean,
    val schemaPath: String
) : IConfigSection
