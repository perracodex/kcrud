/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.access.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import kcrud.access.actor.service.DefaultActorFactory
import kcrud.access.credential.CredentialService
import kcrud.access.rbac.plugin.annotation.RbacAPI
import kcrud.access.rbac.views.RbacLoginView
import kcrud.access.system.SessionContextFactory
import kcrud.base.env.SessionContext
import org.koin.ktor.ext.inject

/**
 * Refreshes the default actors, and configures the RBAC form login authentication.
 *
 * Demonstrates how to use form-base authentication, in which case
 * principal are not propagated across different requests, so we
 * must use sessions to store the actor information.
 *
 * See: [Basic Authentication Documentation](https://ktor.io/docs/basic.html)
 */
@OptIn(RbacAPI::class)
fun Application.configureRbac() {

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
                val credentialService: CredentialService by inject()
                val userIdPrincipal: UserIdPrincipal? = credentialService.authenticate(credential = credential)

                userIdPrincipal?.let { principal ->
                    SessionContextFactory.from(username = principal.name)?.let { sessionContext ->
                        this.sessions.set(name = SessionContext.SESSION_NAME, value = sessionContext)
                        return@validate sessionContext
                    }
                }

                this.sessions.clear(name = SessionContext.SESSION_NAME)
                null
            }
        }
    }
}
