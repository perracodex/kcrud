/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.errors

import io.ktor.http.*

/**
 * Abstract base class for representing application concrete errors.
 *
 * @property status The [HttpStatusCode] associated with this error.
 * @property code A unique code identifying the type of error.
 * @property description A human-readable description of the error.
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
