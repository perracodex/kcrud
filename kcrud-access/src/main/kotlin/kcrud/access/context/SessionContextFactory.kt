/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.access.context

import com.auth0.jwt.JWT
import com.auth0.jwt.exceptions.JWTDecodeException
import com.auth0.jwt.interfaces.DecodedJWT
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import kcrud.access.credential.CredentialService
import kcrud.access.plugins.configureBasicAuthentication
import kcrud.access.plugins.configureJwtAuthentication
import kcrud.access.plugins.configureOAuthAuthentication
import kcrud.access.rbac.service.RbacService
import kcrud.base.env.SessionContext
import kcrud.base.env.Tracer
import kcrud.base.settings.AppSettings
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Provides factory methods for constructing [SessionContext] instances from various authentication credential flows.
 * This object is primarily utilized by authentication mechanisms like JWT and OAuth to validate credentials
 * and generate [SessionContext] instances. These instances are crucial for populating the [ApplicationCall]
 * with session details and actor-specific information throughout the lifecycle of an API call.
 *
 * Using this factory ensures that all authentication methods adhere to a consistent approach in
 * constructing [SessionContext] instances, which is vital for security and traceability within the application.
 *
 * @see configureJwtAuthentication
 * @see configureBasicAuthentication
 * @see configureOAuthAuthentication
 */
internal object SessionContextFactory : KoinComponent {
    private val tracer = Tracer<SessionContextFactory>()

    /**
     * Creates a [SessionContext] instance from a JWT [JWTCredential].
     *
     * @param jwtCredential The [JWTCredential] containing actor-related claims.
     * @return A [SessionContext] instance if actor details and validations pass; null otherwise.
     */
    fun from(jwtCredential: JWTCredential): SessionContext? {
        // Check if the JWT audience claim matches the configured audience.
        // This ensures the token is intended for the application.
        if (!jwtCredential.payload.audience.contains(AppSettings.security.jwtAuth.audience)) {
            tracer.error("Invalid JWT audience: ${jwtCredential.payload.audience}")
            return null
        }

        // Check if the JWT issuer matches the configured issuer.
        // This ensures the token was issued by a trusted source.
        if (jwtCredential.payload.issuer != AppSettings.security.jwtAuth.issuer) {
            tracer.error("Invalid JWT issuer: ${jwtCredential.payload.issuer}")
            return null
        }

        // Extract the serialized SessionContext from the JWT claims.
        // This payload contains key session details serialized as a string,
        // intended for reconstructing the SessionContext.
        // If absent or blank, it indicates the JWT does not contain the required SessionContext data.
        val payload: String? = jwtCredential.payload.getClaim(SessionContext.CLAIM_KEY)?.asString()
        if (payload.isNullOrBlank()) {
            tracer.error("Missing JWT payload.")
            return null
        }

        // Return a fully constructed SessionContext for the reconstructed payload.
        return payload.let {
            Json.decodeFromString<SessionContext>(string = it).run {
                SessionContext(
                    actorId = actorId,
                    username = username,
                    roleId = roleId,
                    schema = schema
                )
            }
        }
    }

    /**
     * Creates a [SessionContext] by authenticating a [UserPasswordCredential].
     * Authenticates the actor's credentials and retrieves actor details from the database.
     *
     * @param credential The [UserPasswordCredential] of the actor attempting to authenticate.
     * @return A [SessionContext] instance if actor details and validations pass; null otherwise.
     */
    suspend fun from(credential: UserPasswordCredential): SessionContext? {
        // Resolve the UserIdPrincipal. Return null if the authentication fails to provide it.
        val credentialService: CredentialService by inject()
        val userIdPrincipal: UserIdPrincipal = credentialService.authenticate(credential = credential) ?: run {
            tracer.error("Failed to resolve UserIdPrincipal. Invalid credentials.")
            return null
        }

        // Resolve the actor. Return null if no actor corresponds to the provided username.
        val rbacService: RbacService by inject()
        rbacService.findActorByUsername(username = userIdPrincipal.name)?.let { actorDetails ->
            return SessionContext(
                actorId = actorDetails.id,
                username = actorDetails.username,
                roleId = actorDetails.roleId
            )
        } ?: run {
            tracer.error("No actor found for username: ${userIdPrincipal.name}")
            return null
        }
    }

    /**
     * Creates a [SessionContext] instance from an OAuth [OAuthAccessTokenResponse.OAuth2].
     *
     * @param oauth2 The OAuth [OAuthAccessTokenResponse.OAuth2] containing actor-related claims.
     * @return A [SessionContext] instance if the auth token is valid; null otherwise.
     */
    fun from(oauth2: OAuthAccessTokenResponse.OAuth2): SessionContext? {
        // Resolve the JWT.
        val jwt: DecodedJWT = try {
            JWT.decode(oauth2.extraParameters["id_token"] as String).also { decodedJwt ->
                // Validate JWT immediately after decoding.
                if (decodedJwt.subject.isNullOrBlank()) {
                    tracer.error("Invalid OAuth token: Missing subject.")
                    return null
                }
            }
        } catch (e: JWTDecodeException) {
            tracer.error("Invalid OAuth token. ${e.message}")
            return null
        }

        // Ensure the OAuth issuer matches expected configuration.
        if (!AppSettings.security.oAuth.authorizeUrl.startsWith(jwt.issuer)) {
            tracer.error("Invalid OAuth issuer: ${jwt.issuer}")
            return null
        }

        // Verify the audience to ensure the token was intended for this application.
        if (jwt.audience.isEmpty() || jwt.audience[0] != AppSettings.security.oAuth.clientId) {
            tracer.error("Invalid OAuth audience: ${jwt.audience}")
            return null
        }

        // Resolve the username. Return null if username claim is missing or blank.
        val username: String = jwt.claims["name"]?.asString()?.takeIf { it.isNotBlank() } ?: run {
            tracer.error("Invalid OAuth username: Empty or missing username.")
            return null
        }

        // Resolve the actor. Return null if no actor corresponds to the provided username.
        val rbacService: RbacService by inject()
        rbacService.findActorByUsername(username = username)?.let { actorDetails ->
            return SessionContext(
                actorId = actorDetails.id,
                username = actorDetails.username,
                roleId = actorDetails.roleId
            )
        } ?: run {
            tracer.error("No actor found for username: $username")
            return null
        }
    }
}
