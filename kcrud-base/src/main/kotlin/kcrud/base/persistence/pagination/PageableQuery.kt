/*
 * Copyright (c) 2023-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.persistence.pagination

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.Table
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

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
 * Handles the determination and application of column-based ordering for database
 * queries according to the provided [Pageable] object.
 *
 * Reflection is used to dynamically resolve column references from field names
 * and employing caching to optimize this process reducing the overhead
 * of repetitive reflection.
 *
 * Must consider that this approach assumes that the first field name found in the
 * set of tables that compose a query, is the one to be used for the ordering.
 * So, if a query targets multiple tables, in which some of them have the same field name,
 * a different approach should be used. For example by using a prefix for the field name
 * that identifies the table, instead of passing only the field name.
 *
 * It also ensures correct column resolution across different tables, for better
 * handling identical names in different tables.
 */
private object QueryOrderingHelper {

    // Cache storing column references with table class and column name as the key.
    // Used to optimize the reflection process of finding columns.
    private val columnCache: MutableMap<TableColumnKey, Column<*>> = ConcurrentHashMap()

    /**
     * Applies ordering to a query based on the provided Pageable object.
     * Orders the query according to the fields and directions specified in Pageable.
     *
     * @param query The query to apply ordering to.
     * @param pageable An optional Pageable object containing ordering information.
     */
    fun applyOrder(query: Query, pageable: Pageable?) {
        pageable?.sort?.forEach { order ->
            val column: Column<*> = getSortColumn(targets = query.targets, fieldName = order.field)
            val sortOrder: SortOrder = SortOrder.ASC.takeIf { (order.direction == Pageable.Direction.ASC) } ?: SortOrder.DESC
            query.orderBy(column to sortOrder)
        }
    }

    /**
     * Iterates over a list of tables to find a column matching the specified field name.
     *
     * @param targets A list of tables to search for the column.
     * @param fieldName The name of the field representing the column.
     * @return The found Column reference.
     * @throws IllegalArgumentException If the column is not found in any of the tables.
     */
    private fun getSortColumn(targets: List<Table>, fieldName: String): Column<*> {
        for (table in targets) {
            try {
                return findColumn(table = table, fieldName = fieldName)
            } catch (e: IllegalArgumentException) {
                // Ignore and try the next table.
            }
        }

        throw IllegalArgumentException("Invalid sort column: $fieldName")
    }

    /**
     * Retrieves a column reference for a given table and field name.
     * Uses cached data or performs reflection if not already cached.
     *
     * @param table The table to search for the column.
     * @param fieldName The name of the field representing the column.
     * @return The Column reference from the table.
     * @throws IllegalArgumentException If the column is not found in the table.
     */
    private fun findColumn(table: Table, fieldName: String): Column<*> {
        val tableClass: KClass<out Table> = table::class
        val cacheKey: TableColumnKey = tableClass to fieldName.lowercase()

        // Retrieve from cache, or use reflection to find the column and cache it.
        return columnCache.getOrPut(key = cacheKey) {
            tableClass.memberProperties
                .firstOrNull {
                    it.name.equals(other = fieldName, ignoreCase = true) &&
                            it.returnType.classifier == Column::class
                }
                ?.getter?.call(table) as? Column<*>
                ?: throw IllegalArgumentException("Invalid sort column: $fieldName")
        }
    }
}

/**
 * Represents a key for the column cache.
 * Contains the table class and column name.
 * Defined as a typealias for better readability.
 * The key is composed of the table class and column name.
 */
private typealias TableColumnKey = Pair<KClass<*>, String>
