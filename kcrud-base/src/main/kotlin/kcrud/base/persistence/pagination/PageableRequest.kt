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
 * If the field name does not include a table prefix, and ambiguity arises due to multiple tables
 * sharing the same field name, an exception is raised. To avoid this, it is recommended to prefix
 * field names with table names for clarity and precision.
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
    if ((pageIndex == null).xor(other = pageSize == null)) {
        throw PaginationError.InvalidPageablePair()
    }

    // Retrieve the 'sort' parameters. Each can contain a field name and a sort direction.
    val sortParameters: List<String>? = parameters.getAll(name = "sort")

    // If no parameters are provided, means no pagination is requested.
    if (pageIndex == null && sortParameters.isNullOrEmpty()) {
        return null
    }

    // Parse sorting parameters into a list of Sort directives.
    val sort: List<Pageable.Sort>? = sortParameters?.let {
        SortParser.getSortDirectives(sortParameters = sortParameters)
    }

    return Pageable(
        page = pageIndex ?: 0,
        size = pageSize ?: 0,
        sort = sort
    )
}

private object SortParser {
    /** Delimiter used to split the sort parameter into field name and direction (e.g., "fieldName,ASC"). */
    private const val SORT_SEGMENT_DELIMITER: Char = ','

    /** Delimiter used to split a field segment into table name and field name (e.g., "table.fieldName"). */
    private const val FIELD_SEGMENT_DELIMITER: Char = '.'

    /**
     * Index position for the field name in a split sort segment
     * (assuming the segment is split by [SORT_SEGMENT_DELIMITER]).
     * For example, in the segment "fieldName,ASC", the field name is at index 0.
     */
    private const val FIELD_SEGMENT_INDEX: Int = 0

    /**
     * Index position for the direction (ASC or DESC) in a split sort segment.
     * For example, in the segment "fieldName,ASC", the direction is at index 1
     * corresponding to the value "ASC".
     */
    private const val DIRECTION_SEGMENT_INDEX: Int = 1

    /**
     * Index position for the table name in a split field segment
     * (assuming the segment is split by [FIELD_SEGMENT_DELIMITER]).
     * For example, in the segment "table.fieldName", the table name is at index 0.
     */
    private const val TABLE_NAME_INDEX: Int = 0

    /**
     * Index position for the field name in a split field segment.
     * For example, in the segment "table.fieldName", the field name is at index 1.
     */
    private const val FIELD_NAME_INDEX: Int = 1

    /**
     * Parses the sorting parameters into a list of [Pageable.Sort] directives.
     *
     * @param sortParameters The list of sorting parameters to parse.
     * @return A list of [Pageable.Sort] directives representing the sorting configuration.
     */
    fun getSortDirectives(sortParameters: List<String>): List<Pageable.Sort>? {
        return sortParameters.mapNotNull { parameter ->
            if (parameter.isBlank()) {
                throw PaginationError.MissingSortDirective()
            }

            val sortSegments: List<String> = parameter.split(SORT_SEGMENT_DELIMITER)
                .map(String::trim)

            return@mapNotNull if (sortSegments.isEmpty()) {
                null
            } else {
                // Resolve the table and field names from the field segment.
                val fieldSegment: String = sortSegments[FIELD_SEGMENT_INDEX]
                val tableColumnPair: TableColumnPair = parseTableAndField(segment = fieldSegment)

                // Resolve the sorting direction from the segment.
                val direction: Pageable.Direction = if (sortSegments.size >= 2) {
                    runCatching {
                        Pageable.Direction.valueOf(sortSegments[DIRECTION_SEGMENT_INDEX].uppercase())
                    }.getOrElse {
                        throw PaginationError.InvalidOrderDirection(direction = sortSegments[DIRECTION_SEGMENT_INDEX])
                    }
                } else {
                    // If no direction is specified, default to ascending.
                    Pageable.Direction.ASC
                }

                Pageable.Sort(
                    table = tableColumnPair.table,
                    field = tableColumnPair.field,
                    direction = direction
                )
            }
        }.takeIf { sortDirectives ->
            sortDirectives.isNotEmpty()
        }
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
}
