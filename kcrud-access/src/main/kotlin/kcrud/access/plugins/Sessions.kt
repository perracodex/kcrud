/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.access.plugins

import io.ktor.server.application.*
import io.ktor.server.sessions.*
import kcrud.core.context.SessionContext
import kcrud.core.plugins.Uuid
import kcrud.core.security.utils.EncryptionUtils.toByteKey
import kcrud.core.settings.AppSettings
import kcrud.core.settings.config.sections.security.sections.EncryptionSettings

/**
 * Configure the [Sessions] plugin.
 *
 * The [Sessions] plugin provides a mechanism to persist data between different HTTP requests.
 * Typical use cases include storing a logged-in Actor's ID.
 *
 * #### References
 * - [Sessions](https://ktor.io/docs/server-sessions.html)
 */
public fun Application.configureSessions() {
    val spec: EncryptionSettings.Spec = AppSettings.security.encryption.atTransit
    val encryptionKey: ByteArray = spec.key.toByteKey(length = 16)
    val signKey: ByteArray = spec.sign.toByteKey(length = 32)

    install(plugin = Sessions) {
        // Configured to store the actor ID in the session cookie.
        // If more information is needed, then a serializable data class could be used instead of actor Uuid.
        // In such a case, the SessionContextFactory in charge of parsing the session would need to be updated
        // too besides the next line.
        cookie<Uuid>(name = SessionContext.SESSION_NAME) {
            val session = SessionTransportTransformerEncrypt(
                encryptionKey = encryptionKey,
                signKey = signKey
            )
            transform(transformer = session)
        }
    }
}
