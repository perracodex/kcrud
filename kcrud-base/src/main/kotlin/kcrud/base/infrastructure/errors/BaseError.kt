/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.infrastructure.errors

import io.ktor.http.*

/**
 * Abstract base class for representing application concrete errors.
 *
 * @param status The [HttpStatusCode] associated with this error.
 * @param code A unique code identifying the type of error.
 * @param description A human-readable description of the error.
 */
abstract class BaseError(
    val code: String,
    val status: HttpStatusCode,
    val description: String
) {
    /**
     * Throw a [KcrudException].
     *
     * @param reason An optional human-readable reason for the error, providing more context.
     * @param cause An optional underlying cause of the error, if any.
     */
    fun raise(reason: String? = null, cause: Throwable? = null): Nothing {
        throw KcrudException(error = this, reason = reason, cause = cause)
    }
}
