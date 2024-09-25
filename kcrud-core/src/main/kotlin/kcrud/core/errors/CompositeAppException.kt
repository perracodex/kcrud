/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.core.errors

import kotlinx.serialization.Serializable

/**
 * Composite exception to aggregate multiple [AppException] instances
 * into a single exception, facilitating the handling and reporting of various errors.
 * Useful for scenarios requiring multiple validations, or when multiple errors need
 * to be reported simultaneously.
 *
 * @property errors A list of [AppException] instances, each representing an individual error.
 */
public class CompositeAppException(
    public val errors: List<AppException>
) : Exception(buildMessage(errors)) {

    /**
     * Returns the concatenated list of error messages from
     * all the encapsulated [AppException] instances.
     */
    public fun messageDetail(): String {
        return errors.joinToString(separator = "\n") { it.messageDetail() }
    }

    /**
     * Data class representing a serializable list of [AppException.Response] instances,
     * encapsulating the structured error information that can be sent in an HTTP response.
     *
     * @property errors The list of [AppException.Response] instances that occurred.
     */
    @Serializable
    public data class Responses(
        val errors: List<AppException.Response>
    )

    private companion object {
        fun buildMessage(errors: List<AppException>): String {
            return "Multiple application errors occurred.\n" + errors.joinToString(separator = "\n") { it.messageDetail() }
        }
    }
}
