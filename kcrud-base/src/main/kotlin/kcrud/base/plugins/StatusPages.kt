/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import kcrud.base.env.Tracer
import kcrud.base.errors.AppException
import kcrud.base.settings.AppSettings
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Install the [StatusPages] feature for handling HTTP status codes.
 *
 * The [StatusPages] plugin allows Ktor applications to respond appropriately
 * to any failure state based on a thrown exception or status code.
 *
 * See: [Ktor Status Pages Documentation](https://ktor.io/docs/server-status-pages.html)
 */
public fun Application.configureStatusPages() {
    val tracer = Tracer<Application>()

    install(plugin = StatusPages) {
        // Custom application exceptions.
        exception<AppException> { call: ApplicationCall, cause ->
            tracer.error(message = cause.messageDetail(), cause = cause)
            call.respondError(cause = cause)
        }

        // Handle 401 Unauthorized status.
        status(HttpStatusCode.Unauthorized) { call: ApplicationCall, status: HttpStatusCode ->
            // Add WWW-Authenticate header to the response, indicating Basic Authentication is required.
            // This is specific to Basic Authentication, doesn't affect JWT.
            val realm: String = AppSettings.security.basicAuth.realm
            call.response.header(name = HttpHeaders.WWWAuthenticate, value = "Basic realm=\"${realm}\"")

            // Respond with 401 Unauthorized status code.
            val message = "$status | Use either admin/admin or guest/guest."
            call.respond(status = HttpStatusCode.Unauthorized, message = message)
        }

        // Security exception handling.
        status(HttpStatusCode.MethodNotAllowed) { call: ApplicationCall, status: HttpStatusCode ->
            call.respond(status = HttpStatusCode.MethodNotAllowed, message = "$status")
        }

        // Bad request exception handling.
        exception<BadRequestException> { call: ApplicationCall, cause: Throwable ->
            tracer.error(message = cause.message, cause = cause)
            val message: String = buildErrorMessage(cause)
            call.respond(status = HttpStatusCode.BadRequest, message = message)
        }

        // Additional exception handling.
        exception<IllegalArgumentException> { call: ApplicationCall, cause: Throwable ->
            tracer.error(message = cause.message, cause = cause)
            val message: String = buildErrorMessage(throwable = cause)
            call.respond(status = HttpStatusCode.BadRequest, message = message)
        }
        exception<NotFoundException> { call: ApplicationCall, cause: Throwable ->
            tracer.error(message = cause.message, cause = cause)
            val message: String = buildErrorMessage(throwable = cause)
            call.respond(status = HttpStatusCode.NotFound, message = message)
        }
        exception<Throwable> { call: ApplicationCall, cause: Throwable ->
            tracer.error(message = cause.message, cause = cause)
            call.respond(status = HttpStatusCode.InternalServerError, message = HttpStatusCode.InternalServerError.description)
        }
    }
}

/**
 * Used to notify custom exceptions to the client.
 */
private suspend fun ApplicationCall.respondError(cause: AppException) {
    // Set the ETag header with the error code.
    this.response.header(name = HttpHeaders.ETag, value = cause.code)

    // Serialize the error response.
    val json: String = Json.encodeToString<AppException.ErrorResponse>(value = cause.toErrorResponse())

    // Send the serialized error response.
    this.respondText(
        text = json,
        contentType = ContentType.Application.Json,
        status = cause.status
    )
}

/**
 * Builds a detailed error message by extracting the first two unique messages from the chain of causes
 * of the provided exception, focusing on initial error points that are most relevant for diagnostics.
 *
 * @param throwable The initial throwable from which to start extracting the messages.
 * @return A detailed error message string, comprised of the first two unique messages, if available.
 */
private fun buildErrorMessage(throwable: Throwable): String {
    // Use a set to keep track of unique messages.
    val uniqueMessages = linkedSetOf<String>()

    // Iterate through the exception chain and collect unique messages until we have two.
    generateSequence(throwable) { it.cause }.forEach { currentCause ->
        // Add message if it is unique and we don't yet have two messages.
        if (uniqueMessages.size < 2) {
            currentCause.message?.let { message ->
                if (!uniqueMessages.contains(message)) {
                    uniqueMessages.add(message)
                }
            }
        }
    }

    // Join the collected messages with "Caused by:" if there are exactly two,
    // or just return the single message.
    return uniqueMessages.joinToString(separator = " Caused by: ")
}
