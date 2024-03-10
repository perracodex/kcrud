/*
 * Copyright (c) 2023-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import kcrud.base.admin.rbac.plugin.annotation.RbacAPI
import kcrud.base.admin.rbac.views.RbacLoginView
import kcrud.base.infrastructure.env.SessionContext
import kcrud.base.security.service.CredentialService
import org.koin.ktor.ext.inject

/**
 * Configures the RBAC form login authentication.
 *
 * Demonstrates how to use form-base authentication, in which case
 * principal are not propagated across different requests, so we
 * must use sessions to store the actor information.
 *
 * See: [Basic Authentication Documentation](https://ktor.io/docs/basic.html)
 */
@OptIn(RbacAPI::class)
fun Application.configureRbacLoginAuthentication() {

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
                    SessionContext.from(username = principal.name)?.let { sessionContext ->
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
