/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.infrastructure.utils

import io.ktor.http.*
import kcrud.base.settings.AppSettings

/**
 * Utility object for network-related functionalities.
 */
object NetworkUtils {
    private val tracer = Tracer<NetworkUtils>()

    private const val LISTEN_ALL_IPS: String = "0.0.0.0"
    private val SECURE_PROTOCOL: URLProtocol = URLProtocol.HTTPS
    private val INSECURE_PROTOCOL: URLProtocol = URLProtocol.HTTP

    /**
     * Logs multiple endpoints with a specified reason.
     *
     * Logs are formatted to full URLs.
     *
     * @param reason A description of why these endpoints are being logged, for context.
     * @param endpoints The list of endpoints to be logged.
     */
    fun logEndpoints(reason: String, endpoints: List<String>) {
        val url: Url? = getServerUrl()
        tracer.info("$reason:")
        endpoints.forEach { endpoint ->
            tracer.info("$url/$endpoint")
        }
    }

    fun getServerUrl(): Url? {
        val host: String = AppSettings.deployment.host
        var url: Url? = null

        if (host != LISTEN_ALL_IPS) {
            val port: Int = getPort()
            val protocol: URLProtocol = getProtocol()
            url = URLBuilder(protocol = protocol, host = host, port = port).build()
        }

        return url
    }

    private fun getPort(): Int {
        return if (AppSettings.security.useSecureConnection)
            AppSettings.deployment.sslPort
        else
            AppSettings.deployment.port
    }

    fun getProtocol(): URLProtocol {
        return if (AppSettings.security.useSecureConnection)
            SECURE_PROTOCOL
        else
            INSECURE_PROTOCOL
    }

    fun isSecureProtocol(protocol: String): Boolean {
        return (protocol == SECURE_PROTOCOL.name)
    }

    fun isInsecurePort(ports: List<Int?>): Boolean {
        ports.forEach { port ->
            if (port == null || port == 0 || port == 8080) {
                return true
            }
        }
        return false
    }
}
