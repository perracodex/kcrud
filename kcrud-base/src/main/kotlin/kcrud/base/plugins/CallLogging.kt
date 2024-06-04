/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.request.*
import kcrud.base.env.SessionContext
import kcrud.base.security.snowflake.SnowflakeFactory
import kcrud.base.settings.AppSettings
import org.slf4j.event.Level

/**
 * Configures logging within the application using CallLogging and CallId plugins.
 *
 * [CallLogging] is responsible for logging details about incoming HTTP requests and responses,
 * which includes URL, headers, response status, and more. It can be tailored to log specific
 * request attributes and supports including the call ID in logs.
 *
 * [CallId] generates or retrieves a unique identifier for each request, facilitating
 * end-to-end request tracing and correlation in logs, essential for debugging and monitoring.
 *
 * See: [CallLogging Documentation](https://ktor.io/docs/server-call-logging.html)
 *
 * See: [CallId Documentation](https://ktor.io/docs/server-call-id.html)
 */
fun Application.configureCallLogging() {

    // Set the machine ID used for generating Snowflake IDs.
    SnowflakeFactory.setMachineId(id = AppSettings.runtime.machineId)

    install(plugin = CallLogging) {
        level = Level.INFO

        // Integrates the unique call ID into the Mapped Diagnostic Context (MDC) for logging.
        // This allows the call ID to be included in each log entry, linking logs to specific requests.
        callIdMdc(name = "id")

        // Format the log message to include the call ID, context details, and processing time.
        format { call ->
            val sessionContext: SessionContext? = call.principal<SessionContext>()
            val callDurationMs: Long = call.processingTimeMillis()

            "Call Metric: [${call.request.origin.remoteHost}] " +
                    "${call.request.httpMethod.value} - ${call.request.path()} " +
                    "- by '$sessionContext' - ${callDurationMs}ms"
        }
    }

    install(plugin = CallId) {
        // Generates a unique ID for each call. This ID is used for request tracing and logging.
        // Must be added to the logback.xml file to be included in logs. See %X{id} in logback.xml.
        generate {
            SnowflakeFactory.nextId()
        }

        // Optionally we can also include the IDs to the response headers,
        // so that it can be retrieved by the client for tracing.
        replyToHeader(headerName = HttpHeaders.XRequestId)
    }
}
