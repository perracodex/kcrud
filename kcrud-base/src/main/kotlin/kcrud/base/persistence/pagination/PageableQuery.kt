/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.persistence.pagination

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.Table

/**
 * Extension function to apply pagination to a database [Query] based on the provided [Pageable] object.
 *
 * If Pageable is not null, then first applies the chosen sorting order (if included in the pageable),
 * and after it calculates the start index based on the page and size, to finally apply it
 * as a limit to the query.
 *
 * If the page size is 0, then no limit is applied and all records are returned.
 *
 * If [Pageable] is null, the original query is returned without any changes.
 *
 * @param pageable An optional [Pageable] object containing pagination information.
 * @return The Query with pagination applied if Pageable is provided, otherwise the original Query.
 */
fun Query.applyPagination(pageable: Pageable?): Query {
    pageable?.let {
        QueryOrderingHelper.applyOrder(query = this, pageable = pageable)

        if (it.size > 0) {
            val startIndex: Int = it.page * it.size
            this.limit(n = it.size, offset = startIndex.toLong())
        }
    }

    return this
}

/**
 * Handles the determination and application of column-based ordering
 * for database queries according to the provided [Pageable] object.
 */
private object QueryOrderingHelper {

    private val columnCache = mutableMapOf<String, Column<*>>()

    /**
     * Applies ordering to a query based on the provided Pageable object.
     * Orders the query according to the fields and directions specified in Pageable.
     *
     * @param query The query to apply ordering to.
     * @param pageable An optional Pageable object containing ordering information.
     */
    fun applyOrder(query: Query, pageable: Pageable) {
        pageable.sort?.forEach { order ->
            val cacheKey: String = buildCacheKey(targets = query.targets, tableName = order.table, fieldName = order.field)
            val column: Column<*> = columnCache.getOrPut(cacheKey) {
                resolveColumn(query = query, tableName = order.table, fieldName = order.field)
            }
            val sortOrder: SortOrder = if (order.direction == Pageable.Direction.ASC) SortOrder.ASC else SortOrder.DESC
            query.orderBy(column to sortOrder)
        }
    }

    /**
     * Builds a unique cache key for a column resolution
     * based on the query targets, table name, and field name.
     *
     * @param targets The list of query targets tables, in which the field is searched.
     * @param tableName Optional table name the field belongs to, in which case the field is only in that table.
     * @param fieldName The name of the field representing the column.
     * @return A unique string representing the cache key.
     */
    private fun buildCacheKey(targets: List<Table>, tableName: String?, fieldName: String): String {
        val targetTableNames: String = if (tableName.isNullOrBlank()) {
            targets.joinToString(separator = ",") { it.tableName }
        } else {
            tableName
        }
        return "$targetTableNames:$fieldName"
    }

    /**
     * Resolves a column reference for a given table and field name.
     *
     * @param query The query containing the tables.
     * @param tableName The name of the table (can be null).
     * @param fieldName The name of the field representing the column.
     * @return The resolved Column reference.
     * @throws IllegalArgumentException If the column is not found in any of the tables.
     */
    private fun resolveColumn(query: Query, tableName: String?, fieldName: String): Column<*> {
        val queryTables: List<Table> = if (tableName.isNullOrBlank()) {
            query.targets
        } else {
            listOf(query.targets.firstOrNull { table ->
                table.tableName.equals(other = tableName, ignoreCase = true)
            } ?: throw IllegalArgumentException("Invalid sort table: $tableName"))
        }

        queryTables.forEach { table ->
            table.columns.firstOrNull { column ->
                column.name.equals(other = fieldName, ignoreCase = true)
            }?.let { column ->
                return column
            }
        }

        throw IllegalArgumentException("Invalid sort column: '$fieldName'. Not found in any of the queried tables.")
    }
}
