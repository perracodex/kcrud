/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.errors

import io.ktor.http.*
import kotlinx.serialization.Serializable

/**
 * The application exception class, directly incorporating HTTP status, error code, and description.
 *
 * @property statusCode The [HttpStatusCode] associated with this error.
 * @property errorCode A unique code identifying the type of error.
 * @property context A context identifier for the error, typically the module or feature where it occurred.
 * @property description A human-readable description of the error.
 * @property reason An optional human-readable reason for the exception, providing more context.
 * @param cause The underlying cause of the exception, if any.
 */
public abstract class AppException(
    public val statusCode: HttpStatusCode,
    public val errorCode: String,
    public val context: String,
    public val description: String,
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
        return "Status: ${statusCode.value} | $errorCode | $context | $description $formattedReason"
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
            description = description,
            reason = reason
        )
    }

    /**
     * Data class representing a serializable error response,
     * encapsulating the structured error information that can be sent in an HTTP response.
     *
     * @property status The HTTP status code associated with the error.
     * @property context A context identifier for the error, typically the module or feature where it occurred.
     * @property code The unique code identifying the error.
     * @property description A brief description of the error.
     * @property reason An optional human-readable reason for the error, providing more context.
     */
    @Serializable
    public data class Response(
        val status: Int,
        val context: String,
        val code: String,
        val description: String,
        val reason: String?
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
