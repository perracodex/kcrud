/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package krud.access.plugins

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import krud.access.context.SessionContextFactory
import krud.core.context.clearContext
import krud.core.context.setContext
import krud.core.settings.AppSettings

/**
 * Configures OAuth authentication.
 *
 * OAuth is an open standard for access delegation.
 * OAuth can be used to authorize users of the application by using external providers,
 * such as Google, Facebook, Twitter, and so on.
 *
 * The oauth provider supports the authorization code flow. Can configure OAuth parameters in one place,
 * and Ktor will automatically make a request to a specified authorization server with the necessary parameters.
 *
 * #### References
 * - [Ktor OAuth Authentication Documentation](https://ktor.io/docs/server-oauth.html)
 * - [OAuth 2.0 Authorization](https://auth0.com/blog/adding-auth0-authorization-to-a-ktor-http-api/)
 */
public fun Application.configureOAuthAuthentication() {

    authentication {
        oauth(name = AppSettings.security.oAuth.providerName) {
            urlProvider = {
                AppSettings.security.oAuth.redirectCallbackUrl
            }

            providerLookup = {
                OAuthServerSettings.OAuth2ServerSettings(
                    name = AppSettings.security.oAuth.providerName,
                    authorizeUrl = AppSettings.security.oAuth.authorizeUrl,
                    accessTokenUrl = AppSettings.security.oAuth.accessTokenUrl,
                    requestMethod = HttpMethod.Post,
                    clientId = AppSettings.security.oAuth.clientId,
                    clientSecret = AppSettings.security.oAuth.clientSecret,
                    defaultScopes = AppSettings.security.oAuth.defaultScopes
                )
            }

            client = HttpClient(engineFactory = CIO)
        }
    }

    routing {
        authenticate(AppSettings.security.oAuth.providerName, optional = !AppSettings.security.isEnabled) {
            get("/oauth/callback") {

                call.principal<OAuthAccessTokenResponse.OAuth2>()?.let { principal ->
                    SessionContextFactory.from(oauth2 = principal)?.let { sessionContext ->
                        call.setContext(sessionContext = sessionContext)
                        call.respondText(text = "You are now logged in through OAuth.")
                        return@get
                    }
                }

                call.clearContext()
                call.respond(message = HttpStatusCode.Unauthorized)
            }
        }
    }
}
