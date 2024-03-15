/*
 * Copyright (c) 2023-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.persistence.pagination

import io.ktor.http.*
import kcrud.base.errors.BaseError
import kcrud.base.errors.ErrorCodeRegistry

/**
 * Pagination concrete errors.
 */
sealed class PaginationError(
    status: HttpStatusCode,
    code: String,
    description: String
) : BaseError(status = status, code = code, description = description) {

    data object InvalidPageablePair : PaginationError(
        status = HttpStatusCode.BadRequest,
        code = "${TAG}IPP",
        description = "Page attributes mismatch. Expected both 'page' and 'size', or none of them."
    )

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
