/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.access.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.sessions.*
import kcrud.access.system.CallContextFactory
import kcrud.base.env.CallContext
import kcrud.base.env.CallContext.Companion.setContext
import kcrud.base.settings.AppSettings

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
                CallContextFactory.from(credential = credential)?.let { callContext ->
                    this.setContext(callContext = callContext)
                    return@validate callContext
                }

                this.sessions.clear(name = CallContext.SESSION_NAME)
                return@validate null
            }
        }
    }
}
