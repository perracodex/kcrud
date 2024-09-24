/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.access.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import kcrud.access.actor.service.DefaultActorFactory
import kcrud.access.context.SessionContextFactory
import kcrud.access.rbac.plugin.annotation.RbacAPI
import kcrud.access.rbac.view.RbacLoginView
import kcrud.core.env.SessionContext
import kcrud.core.env.SessionContext.Companion.setContext

/**
 * Refreshes the default actors, and configures the RBAC form login authentication.
 *
 * Demonstrates how to use form-core authentication, in which case
 * principal are not propagated across different requests, so we
 * must use [Sessions] to store the actor information.
 *
 * See: [Basic Authentication Documentation](https://ktor.io/docs/server-basic-auth.html)
 */
@OptIn(RbacAPI::class)
public fun Application.configureRbac() {

    // Refresh the default actors.
    DefaultActorFactory.refresh()

    // Configure the RBAC form login authentication.
    authentication {
        form(name = RbacLoginView.RBAC_LOGIN_PATH) {
            userParamName = RbacLoginView.KEY_USERNAME
            passwordParamName = RbacLoginView.KEY_PASSWORD

            challenge {
                call.sessions.clear(name = SessionContext.SESSION_NAME)
                call.respondRedirect(url = RbacLoginView.RBAC_LOGIN_PATH)
            }

            validate { credential ->
                SessionContextFactory.from(credential = credential)?.let { sessionContext ->
                    this.setContext(sessionContext = sessionContext)
                    return@validate sessionContext
                }

                this.sessions.clear(name = SessionContext.SESSION_NAME)
                return@validate null
            }
        }
    }
}
