/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.access.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*
import kcrud.access.credential.CredentialService
import kcrud.access.system.SessionContextFactory
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
                    SessionContextFactory.from(username = principal.name)
                }
            }
        }
    }
}
