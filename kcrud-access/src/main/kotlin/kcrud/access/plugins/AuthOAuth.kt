/*
 * Copyright (c) 2024-Present Perraco. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.access.plugins

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kcrud.access.system.SessionContextFactory
import kcrud.base.env.SessionContext
import kcrud.base.settings.AppSettings

/**
 * Configures OAuth authentication.
 *
 * OAuth is an open standard for access delegation.
 * OAuth can be used to authorize users of your application by using external providers,
 * such as Google, Facebook, Twitter, and so on.
 *
 * The oauth provider supports the authorization code flow. You can configure OAuth parameters in one place,
 * and Ktor will automatically make a request to a specified authorization server with the necessary parameters.
 *
 * See: [Ktor OAuth Authentication Documentation](https://ktor.io/docs/server-oauth.html)
 *
 * See: [OAuth 2.0 Authorization](https://auth0.com/blog/adding-auth0-authorization-to-a-ktor-http-api/)
 */
fun Application.configureOAuthAuthentication() {

    authentication {
        oauth(name = AppSettings.security.oauth.providerName) {
            urlProvider = {
                AppSettings.security.oauth.redirectCallbackUrl
            }

            providerLookup = {
                OAuthServerSettings.OAuth2ServerSettings(
                    name = AppSettings.security.oauth.providerName,
                    authorizeUrl = AppSettings.security.oauth.authorizeUrl,
                    accessTokenUrl = AppSettings.security.oauth.accessTokenUrl,
                    requestMethod = HttpMethod.Post,
                    clientId = AppSettings.security.oauth.clientId,
                    clientSecret = AppSettings.security.oauth.clientSecret,
                    defaultScopes = AppSettings.security.oauth.defaultScopes
                )
            }

            client = HttpClient(engineFactory = CIO)
        }
    }

    routing {
        authenticate(AppSettings.security.oauth.providerName, optional = !AppSettings.security.isEnabled) {
            get("/oauth/callback") {

                call.principal<OAuthAccessTokenResponse.OAuth2>()?.let { principal ->
                    SessionContextFactory.from(oauth2 = principal)?.let { sessionContext ->
                        call.sessions.set(name = SessionContext.SESSION_NAME, value = sessionContext)
                        call.respondText("You are now logged in through OAuth.")
                        return@get
                    }
                }

                call.sessions.clear(name = SessionContext.SESSION_NAME)
                call.respond(message = HttpStatusCode.Unauthorized)
            }
        }
    }
}
