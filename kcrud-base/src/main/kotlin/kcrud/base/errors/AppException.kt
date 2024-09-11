/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.errors

import io.ktor.http.*
import kotlinx.serialization.Serializable

/**
 * The application exception class, directly incorporating HTTP status, error code, and description.
 *
 * @param status The HTTP status code associated with this error.
 * @param context A context identifier for the error, typically the module or feature where it occurred.
 * @param code A unique code identifying the type of error.
 * @param description A human-readable description of the error.
 * @param reason An optional human-readable reason for the exception, providing more context.
 * @param cause The underlying cause of the exception, if any.
 */
public abstract class AppException(
    public val status: HttpStatusCode,
    public val context: String,
    public val code: String,
    public val description: String,
    private val reason: String? = null,
    cause: Throwable? = null
) : RuntimeException(
    buildMessage(description = description, reason = reason),
    cause
) {
    /**
     * Generates a detailed message string for this exception, combining the exception segments.
     * @return The detailed message string.
     */
    public fun messageDetail(): String {
        val formattedReason: String = reason?.let { "| $it" } ?: ""
        return "Status: ${status.value} | $context | $code | $description $formattedReason"
    }

    /**
     * Converts this exception into a serializable [ErrorResponse] instance,
     * suitable for sending in an HTTP response.
     * @return The [ErrorResponse] instance representing this exception.
     */
    public fun toErrorResponse(): ErrorResponse {
        return ErrorResponse(
            status = status.value,
            context = context,
            code = code,
            description = description,
            reason = reason
        )
    }

    /**
     * Data class representing a serializable error response,
     * encapsulating the structured error information that can be sent in an HTTP response.
     *
     * @param status The HTTP status code associated with the error.
     * @param context A context identifier for the error, typically the module or feature where it occurred.
     * @param code The unique code identifying the error.
     * @param description A brief description of the error.
     * @param reason An optional human-readable reason for the error, providing more context.
     */
    @Serializable
    public data class ErrorResponse(
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
