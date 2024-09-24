/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.core.utils

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import kcrud.core.env.Tracer
import kcrud.core.settings.AppSettings

/**
 * Utility object for network-related functionalities.
 */
public object NetworkUtils {
    private val tracer = Tracer<NetworkUtils>()

    private const val DEFAULT_PORT: Int = 8080
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
     * @param endpoints A list of endpoint paths (without core URL) to be logged.
     */
    public fun logEndpoints(reason: String, endpoints: List<String>) {
        tracer.info("$reason:")
        val baseUrl: Url = getServerUrl()
        endpoints.forEach { endpoint ->
            val finalUrl: String = URLBuilder(url = baseUrl).apply {
                appendPathSegments(endpoint.trim())
            }.buildString()
            tracer.info(finalUrl)
        }
    }

    /**
     * Constructs the server's core URL based on current deployment settings.
     *
     * This function checks the application's configured host, port, and protocol settings
     * to construct the core URL.
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
     * environment settings should provide the correct core URL.
     *
     * @return The constructed Url object representing the server's core URL.
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
        return if (AppSettings.security.useSecureConnection) {
            AppSettings.deployment.sslPort
        } else {
            AppSettings.deployment.port
        }
    }

    /**
     * Returns the protocol (HTTP or HTTPS) based on the current security configuration.
     *
     * @return The [URLProtocol] representing HTTP or HTTPS.
     */
    public fun getProtocol(): URLProtocol {
        return if (AppSettings.security.useSecureConnection) {
            SECURE_PROTOCOL
        } else {
            INSECURE_PROTOCOL
        }
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
        return (port != 0) && (port != DEFAULT_PORT)
    }

    /**
     * Retrieves the active connectors configurations from the application's server environment.
     * Connectors detail the interfaces through which the server communicates over the network.
     * This function maps each connector type to its configuration details, such as host, port, and security settings.
     *
     * @param environment [ApplicationEnvironment] providing context for accessing the application's runtime environment.
     * @param errors A mutable list to log any errors encountered during connector retrieval.
     * @return A map where each key represents a connector type and the value is a list of its configuration details.
     */
    internal fun getConnectors(environment: ApplicationEnvironment, errors: MutableList<String>): MutableMap<String, List<String>> {
        val connectors: MutableMap<String, List<String>> = mutableMapOf()

        (environment as ApplicationEngineEnvironmentReloading).connectors.forEach { connection ->
            val connectorData: String = connection.type.name
            var attributes: MutableList<String>? = null

            when (connection) {
                is EngineSSLConnectorBuilder -> {
                    attributes = mutableListOf(
                        "host: ${connection.host}",
                        "post: ${connection.port}",
                        "keyStoreType: ${connection.keyStore.type}",
                        "keyStoreProvider: ${connection.keyStore.provider}",
                        "keyStorePath: ${connection.keyStorePath}",
                        "keyAlias: ${connection.keyAlias}",
                        "trustStorePath: ${connection.trustStorePath}",
                        "trustStore: ${connection.trustStore}",
                        "enabledProtocols: ${connection.enabledProtocols}"
                    )
                }

                is EngineConnectorBuilder -> {
                    attributes = mutableListOf(
                        "host: ${connection.host}",
                        "post: ${connection.port}"
                    )
                }
            }

            attributes?.let { data ->
                connectors[connectorData] = data
            } ?: errors.add("Unknown Connector: $connection")
        }

        return connectors
    }
}
