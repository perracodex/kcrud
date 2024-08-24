/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
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
 * The sorting logic accommodates scenarios involving multiple tables. If the sorting field specification
 * includes a table name (denoted by a field name prefixed with the table name and separated by a dot,
 * like "table.fieldName"), the sorting is applied specifically to the identified table and field.
 * This explicit specification prevents ambiguity and ensures accurate sorting when queries involve
 * multiple tables with potentially overlapping field names.
 *
 * If the field name does not include a table prefix, the function applies the sort order to the first
 * matching field found among the query's target tables. It's important to note that without specifying
 * table names, there might be ambiguity in queries targeting multiple tables with identical field names;
 * hence, prefixing field names with table names is recommended for clarity and precision.
 *
 * If no pagination or sorting is requested, the function returns null, indicating the absence of
 * pageable constraints.
 *
 * @return A [Pageable] object containing the pagination and sorting configuration derived from the
 *         request's parameters, or null if no pagination or sorting is requested.
 * @throws PaginationError.InvalidPageablePair if pagination parameters are incomplete.
 * @throws PaginationError.InvalidOrderDirection if a sorting direction is invalid.
 */
public fun ApplicationCall.getPageable(): Pageable? {

    val parameters: Parameters = request.queryParameters
    val pageIndex: Int? = parameters["page"]?.toIntOrNull()
    val pageSize: Int? = parameters["size"]?.toIntOrNull()

    // If only one of the page parameters is provided, raise an error.
    if ((pageIndex == null) != (pageSize == null)) {
        throw PaginationError.InvalidPageablePair()
    }

    // Parse multiple 'sort' parameters. Each can contain a field name and a sort direction.
    val sortParameters: List<String>? = parameters.getAll(name = "sort")

    // If no parameters are provided, means no pagination is requested.
    if (pageIndex == null && sortParameters.isNullOrEmpty()) {
        return null
    }

    // Parse sorting parameters into a list of Sort objects.
    val sort: List<Pageable.Sort>? = sortParameters?.mapNotNull { parameter ->
        val sortSegments: List<String> = parameter.split(SORT_SEGMENT_DELIMITER).map { it.trim() }

        when {
            sortSegments.isNotEmpty() -> {
                val fieldSegment: String = sortSegments[FIELD_SEGMENT_INDEX]
                val tableColumnPair: TableColumnPair = parseTableAndField(segment = fieldSegment)

                val direction: Pageable.Direction = if (sortSegments.size >= 2) {
                    val directionString: String = sortSegments[DIRECTION_SEGMENT_INDEX]
                    try {
                        Pageable.Direction.valueOf(directionString.uppercase())
                    } catch (e: IllegalArgumentException) {
                        throw PaginationError.InvalidOrderDirection(direction = directionString)
                    }
                } else {
                    Pageable.Direction.ASC // Default direction.
                }

                Pageable.Sort(
                    table = tableColumnPair.table,
                    field = tableColumnPair.field,
                    direction = direction
                )
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

/**
 * Parses the table name and field name from a segment of a sort parameter.
 *
 * @param segment The segment of a sort parameter to parse the table and field names from.
 * @return A [TableColumnPair] object containing the table and field names.
 */
private fun parseTableAndField(segment: String): TableColumnPair {
    return if (segment.contains(FIELD_SEGMENT_DELIMITER)) {
        val fieldParts: List<String> = segment.split(FIELD_SEGMENT_DELIMITER)
        TableColumnPair(table = fieldParts[TABLE_NAME_INDEX], field = fieldParts[FIELD_NAME_INDEX])
    } else {
        // No table specified.
        TableColumnPair(table = null, field = segment)
    }
}

/**
 * Represents a table and column name pair.
 *
 * @property table Optional name of the table the field belongs to. Used to avoid ambiguity.
 * @property field The name of the field to sort by.
 */
private data class TableColumnPair(val table: String?, val field: String)

private const val SORT_SEGMENT_DELIMITER: Char = ','
private const val FIELD_SEGMENT_DELIMITER: Char = '.'
private const val FIELD_SEGMENT_INDEX: Int = 0
private const val DIRECTION_SEGMENT_INDEX: Int = 1
private const val TABLE_NAME_INDEX: Int = 0
private const val FIELD_NAME_INDEX: Int = 1