/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.persistence.pagination

import io.ktor.http.*
import kcrud.base.errors.AppException

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
) : AppException(
    status = status,
    context = "PAGINATION",
    code = code,
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
        status = HttpStatusCode.BadRequest,
        code = "AMBIGUOUS_SORT_FIELD",
        description = "Detected ambiguous field: ${sort.field}",
        reason = reason
    )

    /**
     * Error when the page attributes are invalid.
     * This is when only one either the page or size is present.
     * Both must be present or none of them.
     */
    class InvalidPageablePair(reason: String? = null, cause: Throwable? = null) : PaginationError(
        status = HttpStatusCode.BadRequest,
        code = "INVALID_PAGEABLE_PAIR",
        description = "Page attributes mismatch. Expected both 'page' and 'size', or none of them.",
        reason = reason,
        cause = cause
    )

    /**
     * Error when the provided sort direction is invalid.
     *
     * @param direction The sort direction that was provided is not valid.
     */
    class InvalidOrderDirection(direction: String, reason: String? = null, cause: Throwable? = null) : PaginationError(
        status = HttpStatusCode.BadRequest,
        code = "INVALID_ORDER_DIRECTION",
        description = "Ordering sort direction is invalid. Received: '$direction'",
        reason = reason,
        cause = cause
    )

    /**
     * Error when provided sorting field is invalid.
     *
     * @param sort The sort directive that was provided.
     */
    class InvalidSortDirective(sort: Pageable.Sort, reason: String) : PaginationError(
        status = HttpStatusCode.BadRequest,
        code = "INVALID_SORT_DIRECTIVE",
        description = "Unexpected sort directive: $sort",
        reason = reason
    )

    /**
     * Error when the provided sort directive is missing.
     * So, that no field name was specified.
     */
    class MissingSortDirective : PaginationError(
        status = HttpStatusCode.BadRequest,
        code = "MISSING_SORT_DIRECTIVE",
        description = "Must specify a sort field name.",
    )
}
