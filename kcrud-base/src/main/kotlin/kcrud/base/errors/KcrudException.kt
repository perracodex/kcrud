/*
 * Copyright (c) 2024-Present Perracodex. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.errors

import kotlinx.serialization.Serializable

/**
 * The application exception class, wrapping a [BaseError] instance.
 *
 * @property error The [BaseError] details associated with this exception.
 * @property reason An optional human-readable reason for the exception, providing more context.
 * @param cause The underlying cause of the exception, if any.
 */
class KcrudException(
    val error: BaseError,
    private val reason: String? = null,
    cause: Throwable? = null
) : RuntimeException(
    buildMessage(error = error, reason = reason),
    cause
) {
    /**
     * Generates a detailed message string for this exception, combining the exception segments.
     * @return The detailed message string.
     */
    fun messageDetail(): String {
        val formattedReason: String = reason?.let { "| $it" } ?: ""
        return "Status: ${error.status.value} | ${error.code} | ${error.description} $formattedReason"
    }

    /**
     * Data class representing a serializable error response,
     * encapsulating the structured error information that can be sent in an HTTP response.
     *
     * @param status The HTTP status code associated with the error.
     * @param code The unique code identifying the error.
     * @param description A brief description of the error.
     * @param reason An optional human-readable reason for the error, providing more context.
     */
    @Serializable
    data class ErrorResponse(
        val status: Int,
        val code: String,
        val description: String,
        val reason: String?
    )

    /**
     * Converts this exception into a serializable [ErrorResponse] instance,
     * suitable for sending in an HTTP response.
     * @return The [ErrorResponse] instance representing this exception.
     */
    fun toErrorResponse(): ErrorResponse {
        return ErrorResponse(
            status = error.status.value,
            code = error.code,
            description = error.description,
            reason = reason
        )
    }

    companion object {
        /**
         * Builds the final exception message by concatenating the provided error description and reason.
         *
         * @param error The [BaseError] providing the base description of the error.
         * @param reason An optional additional reason to be appended to the error description.
         * @return The concatenated error message.
         */
        private fun buildMessage(error: BaseError, reason: String?): String {
            return (reason?.let { "$it : " } ?: "") + error.description
        }
    }
}

