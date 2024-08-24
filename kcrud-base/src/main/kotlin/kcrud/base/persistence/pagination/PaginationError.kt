/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.persistence.pagination

import io.ktor.http.*
import kcrud.base.errors.AppException
import kcrud.base.errors.ErrorCodeRegistry

/**
 * Pagination concrete errors.
 *
 * @property status The [HttpStatusCode] associated with this error.
 * @property code A unique code identifying the type of error.
 * @property description A human-readable description of the error.
 * @property reason An optional human-readable reason for the exception, providing more context.
 * @property cause The underlying cause of the exception, if any.
 */
internal sealed class PaginationError(
    status: HttpStatusCode,
    code: String,
    description: String,
    reason: String? = null,
    cause: Throwable? = null
) : AppException(status = status, code = code, description = description, reason = reason, cause = cause) {

    /**
     * Error when the page attributes are invalid.
     * This is when only one either the page or size is present.
     * Both must be present or none of them.
     */
    class InvalidPageablePair(reason: String? = null, cause: Throwable? = null) : PaginationError(
        status = HttpStatusCode.BadRequest,
        code = "${TAG}IPP",
        description = "Page attributes mismatch. Expected both 'page' and 'size', or none of them.",
        reason = reason,
        cause = cause
    )

    /**
     * Error when the provided sort direction is invalid.
     *
     * @property direction The sort direction that was provided is not valid.
     */
    class InvalidOrderDirection(val direction: String?, reason: String? = null, cause: Throwable? = null) : PaginationError(
        status = HttpStatusCode.BadRequest,
        code = "${TAG}IOD",
        description = "Ordering sort direction is invalid. Received: '$direction'",
        reason = reason,
        cause = cause
    )

    private companion object {
        const val TAG: String = "PGN."

        init {
            ErrorCodeRegistry.registerTag(tag = TAG)
        }
    }
}
