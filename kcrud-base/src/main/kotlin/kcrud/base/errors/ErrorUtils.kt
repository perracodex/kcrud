/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.errors

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Provides utility functions to assist with error handling and response generation.
 */
public object ErrorUtils {

    /**
     * Builds a detailed error message by extracting the first two unique messages from the chain of causes
     * of the provided exception, focusing on initial error points that are most relevant for diagnostics.
     *
     * @param throwable The initial throwable from which to start extracting the messages.
     * @return A detailed error message string, comprised of the first two unique messages, if available.
     */
    public fun buildMessage(throwable: Throwable): String {
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
}

/**
 * Extension function to build a detailed message string from
 * the given [AppException] instance, and respond it to the client.
 *
 * @param cause The [AppException] instance to respond with.
 */
public suspend fun ApplicationCall.respondError(cause: AppException) {
    // Set the ETag header with the error code.
    this.response.header(name = HttpHeaders.ETag, value = cause.errorCode)

    // Serialize the error response.
    val json: String = Json.encodeToString<AppException.Response>(value = cause.toResponse())

    // Send the serialized error response.
    this.respondText(
        text = json,
        contentType = ContentType.Application.Json,
        status = cause.statusCode
    )
}

/**
 * Extension function to build a detailed message string from the multiple
 * [AppException] instances encapsulated by the given [CompositeAppException] instance,
 * and respond it to the client.
 *
 * @param cause The [CompositeAppException] instance containing multiple errors to respond with.
 */
public suspend fun ApplicationCall.respondError(cause: CompositeAppException) {
    // Set the ETag header with the error codes.
    val etagValue: String = cause.errors.joinToString(separator = ";") { it.errorCode }
    this.response.header(name = HttpHeaders.ETag, value = etagValue)

    val responses: CompositeAppException.Responses = CompositeAppException.Responses(
        errors = cause.errors.map { it.toResponse() }
    )

    // Serialize the error responses.
    val json: String = Json.encodeToString(responses)

    // Although each error has its own status code, we respond with a 400 Bad Request.
    // Clients can inspect the individual error codes in the response body.
    this.respondText(
        text = json,
        contentType = ContentType.Application.Json,
        status = HttpStatusCode.BadRequest
    )
}