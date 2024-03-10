/*
 * Copyright (c) 2023-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*
import kcrud.base.infrastructure.utils.Tracer
import kcrud.base.settings.AppSettings
import kcrud.base.settings.config.sections.CorsSettings

/**
 * Configures [CORS] plugin by setting allowed HTTP methods and headers, permitting credentials,
 * and enabling non-simple content types for more complex operations like file uploads.
 *
 * Note: The 'anyHost' setting for CORS is not recommended for production use.
 *
 * See: [CORS Documentation](https://ktor.io/docs/cors.html)
 */
fun Application.configureCors() {

    val tracer: Tracer = Tracer.byFunction(ref = ::configureCors)

    // Install and configure the CORS feature.
    install(plugin = CORS) {

        // Specify allowed HTTP methods for CORS requests.
        allowMethod(method = HttpMethod.Options)
        allowMethod(method = HttpMethod.Post)
        allowMethod(method = HttpMethod.Get)
        allowMethod(method = HttpMethod.Put)
        allowMethod(method = HttpMethod.Delete)

        // Specify allowed HTTP headers for CORS requests.
        allowHeader(header = HttpHeaders.Authorization)
        allowHeader(header = HttpHeaders.ContentType)

        // Enable inclusion of credentials in CORS requests.
        allowCredentials = true

        // Enable non-simple content types,
        // allowing for more complex operations like file uploads.
        allowNonSimpleContentTypes = true

        // Set the allowed hosts.
        val allowedHosts: List<String> = AppSettings.cors.allowedHosts
        tracer.info("Allowed hosts: $allowedHosts")

        // Host configuration examples:
        //
        // Allow requests from both http and https, so "http://example.com" and "https://example.com".
        // allowHost(host="example.com")
        //
        // Allow requests from "http://example.com:8081" and "https://example.com:8081".
        // allowHost(host="example.com:8081")
        //
        // Allow requests from "http://api.example.com" and "https://api.example.com".
        // allowHost(host="example.com", subDomains = listOf("api"))
        //
        // Allows requests from "http://example.com" and "https://example.com" which have different schemes.
        // allowHost(host="example.com", schemes = listOf("http", "https"))

        if (AppSettings.cors.allowAllHosts()) {
            anyHost()
            tracer.byEnvironment("Allowing all hosts.")
        } else {
            allowedHosts.forEach { entry ->
                val config: CorsSettings.HostConfig = CorsSettings.parse(spec = entry)
                allowHost(
                    host = config.host,
                    schemes = config.schemes,
                    subDomains = config.subDomains
                )
            }
        }
    }
}
