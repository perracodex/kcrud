/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.access.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.sessions.*
import kcrud.access.context.SessionContextFactory
import kcrud.core.env.SessionContext
import kcrud.core.env.SessionContext.Companion.setContext
import kcrud.core.settings.AppSettings

/**
 * Configures the Basic authentication.
 *
 * The Basic authentication scheme is a part of the HTTP framework used for access control and authentication.
 * In this scheme, actor credentials are transmitted as username/password pairs encoded using Base64.
 *
 * See: [Basic Authentication Documentation](https://ktor.io/docs/server-basic-auth.html)
 */
public fun Application.configureBasicAuthentication() {

    authentication {
        basic(name = AppSettings.security.basicAuth.providerName) {
            realm = AppSettings.security.basicAuth.realm

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
