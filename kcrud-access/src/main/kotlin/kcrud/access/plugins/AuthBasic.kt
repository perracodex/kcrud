/*
 * Copyright (c) 2024-Present Perraco. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.access.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.sessions.*
import kcrud.access.system.SessionContextFactory
import kcrud.base.env.SessionContext
import kcrud.base.settings.AppSettings

/**
 * Configures the Basic authentication.
 *
 * The Basic authentication scheme is a part of the HTTP framework used for access control and authentication.
 * In this scheme, actor credentials are transmitted as username/password pairs encoded using Base64.
 *
 * See: [Basic Authentication Documentation](https://ktor.io/docs/server-basic-auth.html)
 */
fun Application.configureBasicAuthentication() {

    authentication {
        basic(name = AppSettings.security.basic.providerName) {
            realm = AppSettings.security.basic.realm

            validate { credential ->
                SessionContextFactory.from(credential = credential)?.let { sessionContext ->
                    this.sessions.set(name = SessionContext.SESSION_NAME, value = sessionContext)
                    return@validate sessionContext
                }

                this.sessions.clear(name = SessionContext.SESSION_NAME)
                null
            }
        }
    }
}
