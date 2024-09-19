/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.persistence.pagination

import io.ktor.http.*
import kcrud.base.errors.AppException

/**
 * Pagination concrete errors.
 *
 * @param statusCode The [HttpStatusCode] associated with this error.
 * @param errorCode A unique code identifying the type of error.
 * @param description A human-readable description of the error.
 * @param reason An optional human-readable reason for the exception, providing more context.
 * @param cause The underlying cause of the exception, if any.
 */
internal sealed class PaginationError(
    statusCode: HttpStatusCode,
    errorCode: String,
    description: String,
    reason: String? = null,
    cause: Throwable? = null
) : AppException(
    statusCode = statusCode,
    errorCode = errorCode,
    context = "PAGINATION",
    description = description,
    reason = reason,
    cause = cause
) {
    /**
     * Error when provided sorting fields are ambiguous as they may exist in multiple tables.
     *
     * @param sort The sort directive that was provided.
     */
    class AmbiguousSortField(sort: Pageable.Sort, reason: String) : PaginationError(
        statusCode = STATUS_CODE,
        errorCode = ERROR_CODE,
        description = "Detected ambiguous field: ${sort.field}",
        reason = reason
    ) {
        companion object {
            val STATUS_CODE: HttpStatusCode = HttpStatusCode.BadRequest
            const val ERROR_CODE: String = "AMBIGUOUS_SORT_FIELD"
        }
    }

    /**
     * Error when the page attributes are invalid.
     * This is when only one either the page or size is present.
     * Both must be present or none of them.
     */
    class InvalidPageablePair(reason: String? = null, cause: Throwable? = null) : PaginationError(
        statusCode = STATUS_CODE,
        errorCode = ERROR_CODE,
        description = "Page attributes mismatch. Expected both 'page' and 'size', or none of them.",
        reason = reason,
        cause = cause
    ) {
        companion object {
            val STATUS_CODE: HttpStatusCode = HttpStatusCode.BadRequest
            const val ERROR_CODE: String = "INVALID_PAGEABLE_PAIR"
        }
    }

    /**
     * Error when the provided sort direction is invalid.
     *
     * @param direction The sort direction that was provided is not valid.
     */
    class InvalidOrderDirection(direction: String, reason: String? = null, cause: Throwable? = null) : PaginationError(
        statusCode = STATUS_CODE,
        errorCode = ERROR_CODE,
        description = "Ordering sort direction is invalid. Received: '$direction'",
        reason = reason,
        cause = cause
    ) {
        companion object {
            val STATUS_CODE: HttpStatusCode = HttpStatusCode.BadRequest
            const val ERROR_CODE: String = "INVALID_ORDER_DIRECTION"
        }
    }

    /**
     * Error when provided sorting field is invalid.
     *
     * @param sort The sort directive that was provided.
     */
    class InvalidSortDirective(sort: Pageable.Sort, reason: String) : PaginationError(
        statusCode = STATUS_CODE,
        errorCode = ERROR_CODE,
        description = "Unexpected sort directive: $sort",
        reason = reason
    ) {
        companion object {
            val STATUS_CODE: HttpStatusCode = HttpStatusCode.BadRequest
            const val ERROR_CODE: String = "INVALID_SORT_DIRECTIVE"
        }
    }

    /**
     * Error when the provided sort directive is missing.
     * So, that no field name was specified.
     */
    class MissingSortDirective : PaginationError(
        statusCode = STATUS_CODE,
        errorCode = ERROR_CODE,
        description = "Must specify a sort field name.",
    ) {
        companion object {
            val STATUS_CODE: HttpStatusCode = HttpStatusCode.BadRequest
            const val ERROR_CODE: String = "MISSING_SORT_DIRECTIVE"
        }
    }
}
