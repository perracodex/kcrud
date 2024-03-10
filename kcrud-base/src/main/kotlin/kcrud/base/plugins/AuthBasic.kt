/*
 * Copyright (c) 2023-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*
import kcrud.base.infrastructure.env.SessionContext
import kcrud.base.security.service.CredentialService
import kcrud.base.settings.AppSettings
import org.koin.ktor.ext.inject

/**
 * Configures the Basic authentication.
 *
 * The Basic authentication scheme is a part of the HTTP framework used for access control and authentication.
 * In this scheme, actor credentials are transmitted as username/password pairs encoded using Base64.
 *
 * See: [Basic Authentication Documentation](https://ktor.io/docs/basic.html)
 */
fun Application.configureBasicAuthentication() {

    authentication {
        basic(name = AppSettings.security.basic.providerName) {
            realm = AppSettings.security.basic.realm

            validate { credential ->
                val credentialService: CredentialService by inject()
                val userIdPrincipal: UserIdPrincipal? = credentialService.authenticate(credential = credential)

                userIdPrincipal?.let { principal ->
                    SessionContext.from(username = principal.name)
                }
            }
        }
    }
}
