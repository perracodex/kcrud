/*
 * Copyright (c) 2023-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.graphql.kgraphql.context

import com.apurebase.kgraphql.Context
import kcrud.base.graphql.kgraphql.annotation.KGraphQLAPI
import kcrud.base.infrastructure.env.SessionContext
import kcrud.base.infrastructure.utils.Tracer

/**
 * Graphql session context hold attributes such as the context actor.
 */
@KGraphQLAPI
class GraphQLEnv(private val context: Context) {
    private val tracer = Tracer<GraphQLEnv>()

    val sessionContext: SessionContext? by lazy {
        val sessionContext: SessionContext? = context.get<SessionContext>()
        sessionContext
    }

    @Suppress("unused")
    fun printActor() {
        sessionContext?.let {
            tracer.info("Context actor: ${it.username}. Role: ${it.roleId}.")
        } ?: tracer.info("No session context found.")
    }
}
