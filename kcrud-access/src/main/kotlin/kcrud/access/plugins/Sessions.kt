/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.access.plugins

import io.ktor.server.application.*
import io.ktor.server.sessions.*
import kcrud.core.env.SessionContext
import kcrud.core.security.utils.EncryptionUtils.toByteKey
import kcrud.core.settings.AppSettings
import kcrud.core.settings.config.sections.security.sections.EncryptionSettings

/**
 * Configure the [Sessions] plugin.
 *
 * The [Sessions] plugin provides a mechanism to persist data between different HTTP requests.
 * Typical use cases include storing a logged-in Actor's ID, the contents of a shopping basket,
 * or keeping actor preferences on the client.
 *
 * In Ktor, is possible can implement sessions by using cookies or custom headers, choose whether to store
 * session data on the server or pass it to the client, sign and encrypt session data and more.
 *
 * See: [Sessions](https://ktor.io/docs/server-sessions.html)
 */
public fun Application.configureSessions() {

    val spec: EncryptionSettings.Spec = AppSettings.security.encryption.atTransit
    val encryptionKey: ByteArray = spec.key.toByteKey(length = 16)
    val signKey: ByteArray = spec.sign.toByteKey(length = 32)

    install(plugin = Sessions) {
        cookie<SessionContext>(name = SessionContext.SESSION_NAME) {
            val session = SessionTransportTransformerEncrypt(
                encryptionKey = encryptionKey,
                signKey = signKey
            )
            transform(transformer = session)
        }
    }
}
