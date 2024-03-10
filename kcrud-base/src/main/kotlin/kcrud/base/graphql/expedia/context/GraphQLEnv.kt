/*
 * Copyright (c) 2023-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.graphql.expedia.context

import graphql.schema.DataFetchingEnvironment
import kcrud.base.graphql.expedia.annotation.ExpediaAPI
import kcrud.base.infrastructure.env.SessionContext
import kcrud.base.infrastructure.utils.Tracer

/**
 * Graphql session context hold attributes such as the context actor.
 */
@ExpediaAPI
class GraphQLEnv(private val env: DataFetchingEnvironment) {
    private val tracer = Tracer<GraphQLEnv>()

    val sessionContext: SessionContext? by lazy {
        val sessionContext: SessionContext? = env.graphQlContext[SessionContext::class]
        sessionContext
    }

    @Suppress("unused")
    fun printActor() {
        sessionContext?.let {
            tracer.info("Context actor: ${it.username}. Role: ${it.roleId}.")
        } ?: tracer.info("No session context found.")
    }
}
