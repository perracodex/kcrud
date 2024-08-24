/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.utils

import io.ktor.http.*
import kcrud.base.env.Tracer
import kcrud.base.settings.AppSettings

/**
 * Utility object for network-related functionalities.
 */
public object NetworkUtils {
    private val tracer = Tracer<NetworkUtils>()

    private const val LISTEN_ALL_INTERFACES: String = "0.0.0.0"
    private val SECURE_PROTOCOL: URLProtocol = URLProtocol.HTTPS
    private val INSECURE_PROTOCOL: URLProtocol = URLProtocol.HTTP

    /**
     * Logs multiple endpoints with a specified reason.
     *
     * This function formats the provided list of endpoints into full URLs and logs them
     * with a specified contextual reason. It is primarily used for logging purposes,
     * to document accessible endpoints under specific conditions or configurations.
     *
     * @param reason A description of why these endpoints are being logged, providing context.
     * @param endpoints A list of endpoint paths (without base URL) to be logged.
     */
    public fun logEndpoints(reason: String, endpoints: List<String>) {
        val url: Url = getServerUrl()
        tracer.info("$reason:")
        endpoints.forEach { endpoint ->
            tracer.info("$url/$endpoint")
        }
    }

    /**
     * Constructs the server's base URL based on current deployment settings.
     *
     * This function checks the application's configured host, port, and protocol settings
     * to construct the base URL.
     *
     * If the application is configured to listen on all interfaces (0.0.0.0),
     * the function substitutes 'localhost' for the host in the constructed URL,
     * which is suitable for local testing and development.
     *
     * In production or other deployment scenarios, it uses the actual host as defined
     * in the application settings.
     *
     * Note: This URL might not be accessible externally if the server is behind a proxy
     * or in a Docker/container environment. In such cases, external configuration or
     * environment settings should provide the correct base URL.
     *
     * @return The constructed Url object representing the server's base URL.
     */
    public fun getServerUrl(): Url {
        val port: Int = getPort()
        val protocol: URLProtocol = getProtocol()
        val host: String = AppSettings.deployment.host

        return if (host == LISTEN_ALL_INTERFACES) {
            URLBuilder(protocol = protocol, host = "localhost", port = port).build()
        } else {
            URLBuilder(protocol = protocol, host = host, port = port).build()
        }
    }

    /**
     * Returns the configured port number for the server based on whether
     * a secure connection (HTTPS) is configured.
     *
     * @return The resolved port number.
     */
    private fun getPort(): Int {
        return if (AppSettings.security.useSecureConnection)
            AppSettings.deployment.sslPort
        else
            AppSettings.deployment.port
    }

    /**
     * Returns the protocol (HTTP or HTTPS) based on the current security configuration.
     *
     * @return The [URLProtocol] representing HTTP or HTTPS.
     */
    public fun getProtocol(): URLProtocol {
        return if (AppSettings.security.useSecureConnection)
            SECURE_PROTOCOL
        else
            INSECURE_PROTOCOL
    }

    /**
     * Evaluates if the given protocol is considered secure.
     *
     * @param protocol The protocol string to evaluate, typically "HTTP" or "HTTPS".
     * @return Whether the protocol is secure.
     */
    public fun isSecureProtocol(protocol: String): Boolean {
        return (protocol == SECURE_PROTOCOL.name)
    }

    /**
     * Evaluates if all the provided port numbers are considered secure.
     *
     * @param ports The list of port numbers to be evaluated.
     * @return True if all ports are considered secure, false otherwise.
     */
    public fun isSecurePort(ports: List<Int?>): Boolean {
        return ports.all { port -> (port != null) && isSecurePort(port = port) }
    }

    /**
     * Evaluates if the given port number is considered secure.
     *
     * @param port The port number to be evaluated.
     * @return True if the port is considered secure, false otherwise.
     */
    public fun isSecurePort(port: Int): Boolean {
        return (port != 0) && (port != 8080)
    }
}
