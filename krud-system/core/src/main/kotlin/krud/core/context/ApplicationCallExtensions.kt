/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package krud.core.context

import io.ktor.server.application.*
import io.ktor.server.sessions.*
import io.ktor.util.*
import krud.core.error.UnauthorizedException
import krud.core.plugins.Uuid
import krud.core.settings.AppSettings
import krud.core.util.toUuid

/**
 * Extension function to add the given [sessionContext] into the [ApplicationCall] attributes,
 * in addition to setting it also in the [Sessions] property to persist between different HTTP requests.
 *
 * @param sessionContext The [SessionContext] to be added.
 * @return The [SessionContext] that was set. Returned to allow for chaining.
 *
 * @see [getContext]
 * @see [getContextOrNull]
 */
public fun ApplicationCall.setContext(sessionContext: SessionContext): SessionContext {
    this.attributes.put(key = SessionContextUtils.sessionContextKey, value = sessionContext)
    this.sessions.set(name = SessionContext.SESSION_NAME, value = sessionContext.actorId)
    return sessionContext
}

/**
 * Extension function to retrieve the [SessionContext] from the [ApplicationCall] attributes,
 * enforcing a secure and standardized retrieval process.
 * The [SessionContext] is typically set by authentication plugins following successful authorizations.
 *
 * Prefer this method over `call.principal<SessionContext>()` to ensure consistent handling
 * of security and session validation.
 *
 * **Retrieval Flow:**
 * 1. Checks for [SessionContext] in the current [ApplicationCall] attributes.
 * 2. Returns it if found; otherwise, it handles the absence based on security settings:
 *    - Throws [UnauthorizedException] if security is enabled.
 *    - Returns a default [SessionContext] with predefined 'empty' values if security is disabled.
 *
 * @return The [SessionContext] if present, or a default one if security is disabled.
 * @throws UnauthorizedException If security is enabled but [SessionContext] is absent.
 *
 * @see [setContext]
 * @see [getContextOrNull]
 */
public fun ApplicationCall.getContext(): SessionContext {
    return getContextOrNull() ?: throw UnauthorizedException("Session context not found.")
}

/**
 * Extension function to retrieve the [SessionContext] from the [ApplicationCall] attributes,
 * enforcing a secure and standardized retrieval process.
 * The [SessionContext] is typically set by authentication plugins following successful authorizations.
 *
 * @return A [SessionContext] representing the authenticated actor;
 * `null` if unauthorized; or a default empty [SessionContext] when security is disabled.
 *
 * @see [setContext]
 * @see [getContext]
 */
public fun ApplicationCall.getContextOrNull(): SessionContext? {
    return this.attributes.getOrNull(key = SessionContextUtils.sessionContextKey)
        ?: SessionContextUtils.emptySessionContext.takeIf { AppSettings.security.isEnabled == false }
}

/**
 * Extension function to clear the [SessionContext] from the [ApplicationCall] attributes and [Sessions].
 */
public fun ApplicationCall.clearContext() {
    this.attributes.remove(key = SessionContextUtils.sessionContextKey)
    this.sessions.clear(name = SessionContext.SESSION_NAME)
}

/**
 * Utility object for handling [SessionContext] operations.
 */
private object SessionContextUtils {
    /**
     * Attribute key for storing the [SessionContext] into the [ApplicationCall] attributes.
     */
    val sessionContextKey: AttributeKey<SessionContext> = AttributeKey(name = "SESSION_CONTEXT")

    /**
     * A default empty [SessionContext] instance. when security is disabled.
     */
    val emptySessionContext: SessionContext by lazy {
        val uuid: Uuid = "00000000-0000-0000-0000-000000000000".toUuid()
        SessionContext(
            actorId = uuid,
            username = "no-actor",
            roleId = uuid
        )
    }
}
