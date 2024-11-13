/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.core.error

import io.ktor.http.*
import io.ktor.server.application.*
import kcrud.core.error.validator.base.ValidationException
import kotlinx.serialization.Serializable

/**
 * Base class for all application-specific exceptions. It provides a structured error response
 * that can be serialized and sent in an HTTP response.
 *
 * If a [cause] is provided, and such is of type [ValidationException], its associated code
 * will be extracted and included in error responses.
 *
 * @property statusCode The [HttpStatusCode] associated with this error.
 * @property errorCode A unique code identifying the type of error.
 * @property context A context identifier for the error, typically the module or feature where it occurred.
 * @property description A human-readable description of the error.
 * @property field Optional field identifier, typically the input field that caused the error.
 * @property reason Optional human-readable reason for the exception, providing more context.
 * @property cause Optional underlying cause of the exception, if any.
 *
 * @see [ApplicationCall.respondError]
 * @see [ValidationException]
 */
public abstract class AppException(
    public val statusCode: HttpStatusCode,
    public val errorCode: String,
    public val context: String,
    public val description: String,
    public val field: String? = null,
    private val reason: String? = null,
    cause: Throwable? = null
) : Exception(
    buildMessage(description = description, reason = reason),
    cause
) {
    /**
     * Generates a detailed message string for this exception, combining the exception segments.
     * @return The detailed message string.
     */
    public fun messageDetail(): String {
        val formattedReason: String = reason?.let { "| $it" } ?: ""
        val formattedCause: String = cause?.let { "| ${it.message}" } ?: ""
        return "Status: ${statusCode.value} | $errorCode | $context | $description $formattedReason $formattedCause"
    }

    /**
     * Converts this exception into a serializable [Response] instance,
     * suitable for sending in an HTTP response.
     * @return The [Response] instance representing this exception.
     */
    public fun toResponse(): Response {
        return Response(
            status = statusCode.value,
            context = context,
            code = errorCode,
            validationCode = (cause as? ValidationException)?.code,
            field = field,
            description = description,
            reason = reason,
            cause = cause?.message
        )
    }

    /**
     * Data class representing a serializable error response,
     * encapsulating the structured error information that can be sent in an HTTP response.
     *
     * @property status The HTTP status code associated with the error.
     * @property context A context identifier for the error, typically the module or feature where it occurred.
     * @property code The unique code identifying the error.
     * @property validationCode Optional validation code associated with the error.
     * @property field Optional field identifier, typically the input field that caused the error.
     * @property description A brief description of the error.
     * @property reason Optional human-readable reason for the error, providing more context.
     * @property cause The underlying cause of the error, if any.
     */
    @Serializable
    public data class Response(
        val status: Int,
        val context: String,
        val code: String,
        val validationCode: String?,
        val field: String?,
        val description: String,
        val reason: String?,
        val cause: String?
    )

    private companion object {
        /**
         * Builds the final exception message by concatenating the provided error description and reason.
         *
         * @param description The base description of the error.
         * @param reason An optional additional reason to be appended to the error description.
         * @return The concatenated error message.
         */
        fun buildMessage(description: String, reason: String?): String {
            return (reason?.let { "$it : " } ?: "") + description
        }
    }
}
