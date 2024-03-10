/*
 * Copyright (c) 2023-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.persistence.pagination

import io.ktor.http.*
import io.ktor.server.application.*

/**
 * Extension function to construct a [Pageable] instance from [ApplicationCall] request parameters.
 *
 * Pagination parameters:
 * - 'page': The index of the page requested (0-based). If not provided, defaults to 0.
 * - 'size': The number of items per page. If not provided, defaults to 0, indicating no pagination.
 *
 * Sorting parameters:
 * - 'sort': A list of strings indicating the fields to sort by and their directions. Each string
 *           should follow the format "fieldName,direction". If 'direction' is omitted, ascending
 *           order is assumed. Multiple 'sort' parameters can be provided for multi-field sorting.
 *
 * A [PaginationError.InvalidPageablePair] is raised if only one pagination parameter
 * ('page' or 'size') is provided without the other.
 * Similarly, a [PaginationError.InvalidOrderDirection] is raised if an invalid direction is
 * specified in any 'sort' parameter.
 *
 * If no pagination or sorting is requested, the function returns null, indicating the absence of
 * pageable constraints.
 *
 * @return A [Pageable] object containing the pagination and sorting configuration derived from the
 *         request's parameters, or null if no pagination or sorting is requested.
 * @throws PaginationError.InvalidPageablePair if pagination parameters are incomplete.
 * @throws PaginationError.InvalidOrderDirection if a sorting direction is invalid.
 */
fun ApplicationCall.getPageable(): Pageable? {
    val parameters: Parameters = request.queryParameters
    val pageIndex: Int? = parameters["page"]?.toIntOrNull()
    val pageSize: Int? = parameters["size"]?.toIntOrNull()

    // If only one of the page parameters is provided, raise an error.
    if ((pageIndex == null) != (pageSize == null)) {
        PaginationError.InvalidPageablePair.raise()
    }

    // Parse multiple 'sort' parameters. Each can contain a field name and a sort direction.
    val sortParameters: List<String>? = parameters.getAll(name = "sort")

    // If no parameters are provided, means no pagination is requested.
    if (pageIndex == null && sortParameters.isNullOrEmpty()) {
        return null
    }

    // Parse sorting parameters into a list of Order objects.
    val sort: List<Pageable.Sort>? = sortParameters?.mapNotNull { parameter ->
        val parts: List<String> = parameter.split(",").map { it.trim() }

        when {
            parts.size >= 2 -> {
                // Only the first two elements of a sort parameter are considered, the rest are ignored.
                val field: String = parts[0] // The first part is always the field name.
                val directionString: String = parts[1] // The second part is always the direction.

                try {
                    val direction: Pageable.Direction = Pageable.Direction.valueOf(directionString.uppercase())
                    Pageable.Sort(field = field, direction = direction)
                } catch (e: IllegalArgumentException) {
                    PaginationError.InvalidOrderDirection(direction = directionString).raise()
                }
            }

            parts.size == 1 -> {
                // Treats a single part as a field name, defaulting direction to ASC.
                Pageable.Sort(field = parts[0], direction = Pageable.Direction.ASC)
            }

            else -> null // This case should never happen due to initial validation.
        }
    }

    return Pageable(
        page = pageIndex ?: 0,
        size = pageSize ?: 0,
        sort = sort
    )
}
