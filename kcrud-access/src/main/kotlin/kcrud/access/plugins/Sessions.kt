/*
 * Copyright (c) 2024-Present Perraco. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.access.plugins

import io.ktor.server.application.*
import io.ktor.server.sessions.*
import kcrud.base.env.SessionContext
import kcrud.base.security.utils.SecurityUtils.to16ByteIV
import kcrud.base.settings.AppSettings

/**
 * Configure the [Sessions] plugin.
 *
 * The [Sessions] plugin provides a mechanism to persist data between different HTTP requests.
 * Typical use cases include storing a logged-in Actor's ID, the contents of a shopping basket,
 * or keeping actor preferences on the client.
 *
 * In Ktor, you can implement sessions by using cookies or custom headers, choose whether to store
 * session data on the server or pass it to the client, sign and encrypt session data and more.
 *
 * See: [Sessions](https://ktor.io/docs/sessions.html)
 */
fun Application.configureSessions() {

    val encryptionKey: ByteArray = AppSettings.security.encryption.key.to16ByteIV()
    val signKey: ByteArray = AppSettings.security.encryption.sign.to16ByteIV()

    install(plugin = Sessions) {
        cookie<SessionContext>(name = SessionContext.SESSION_NAME) {
            val session = SessionTransportTransformerEncrypt(encryptionKey = encryptionKey, signKey = signKey)
            transform(transformer = session)
        }
    }
}
