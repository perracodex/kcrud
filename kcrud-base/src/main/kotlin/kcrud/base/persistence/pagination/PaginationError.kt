/*
 * Copyright (c) 2023-Present Perraco. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.persistence.pagination

import io.ktor.http.*
import kcrud.base.errors.BaseError
import kcrud.base.errors.ErrorCodeRegistry

/**
 * Pagination concrete errors.
 *
 * @property status The [HttpStatusCode] associated with this error.
 * @property code A unique code identifying the type of error.
 * @property description A human-readable description of the error.
 */
sealed class PaginationError(
    status: HttpStatusCode,
    code: String,
    description: String
) : BaseError(status = status, code = code, description = description) {

    /**
     * Error when the page attributes are invalid.
     * This is when only one either the page or size is present.
     * Both must be present or none of them.
     */
    data object InvalidPageablePair : PaginationError(
        status = HttpStatusCode.BadRequest,
        code = "${TAG}IPP",
        description = "Page attributes mismatch. Expected both 'page' and 'size', or none of them."
    )

    /**
     * Error when the provided sort direction is invalid.
     *
     * @property direction The sort direction that was provided is not valid.
     */
    data class InvalidOrderDirection(val direction: String?) : PaginationError(
        status = HttpStatusCode.BadRequest,
        code = "${TAG}IOD",
        description = "Ordering sort direction is invalid. Received: '$direction'"
    )

    companion object {
        private const val TAG: String = "PGN."

        init {
            ErrorCodeRegistry.registerTag(tag = TAG)
        }
    }
}
