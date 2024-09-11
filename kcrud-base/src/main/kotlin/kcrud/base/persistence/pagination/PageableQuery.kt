/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.persistence.pagination

import kcrud.base.env.Tracer
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.Table
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.full.memberProperties

/**
 * Extension function to apply pagination to a database [Query] based on the provided [pageable] directives.
 *
 * If [pageable] is not null, it first applies the chosen sorting order (if included in pageable),
 * then calculates the start index based on the page number and size, and finally applies these as
 * limits to the query.
 *
 * If the page size is zero, no limit is applied, and all records are returned.
 *
 * If [pageable] is null, the original query is returned unchanged.
 *
 * @param pageable An optional [Pageable] object containing pagination information.
 * @return The Query with pagination applied if [pageable] is provided, otherwise the original Query.
 */
public fun Query.paginate(pageable: Pageable?): Query {
    pageable?.let {
        pageable.sort?.let { sort ->
            QueryOrderingHelper.applyOrder(query = this, sort = sort)
        }

        if (pageable.size > 0) {
            val startIndex: Int = pageable.page * pageable.size
            this.limit(n = pageable.size, offset = startIndex.toLong())
        }
    }

    return this
}

/**
 * Handles the determination and application of column-based ordering for database queries.
 */
private object QueryOrderingHelper {
    private val tracer = Tracer<QueryOrderingHelper>()

    /**
     * Cache storing column references.
     * Used to optimize the reflection process of finding table columns.
     *
     * @see generateCacheKey
     */
    private val columnCache: MutableMap<String, Column<*>> = ConcurrentHashMap()

    /**
     * Applies ordering to a query based on the provided list of [Pageable.Sort] directives.
     *
     * @param query The query to apply ordering to.
     * @param sort The list of sorting directives to apply to the query.
     */
    fun applyOrder(query: Query, sort: List<Pageable.Sort>) {
        if (sort.isEmpty()) {
            return
        }

        val queryTables: List<Table> = query.targets.distinct()

        sort.forEach { order ->
            // Filter query targets to find matching tables if a specific table name is provided.
            // If no matching tables are found, throw an InvalidOrderField.
            // If no table is provided, all query targets are considered for column search.
            val targetTables: List<Table> = order.table?.let { tableName ->
                queryTables.filter { table ->
                    table.tableName.equals(other = tableName, ignoreCase = true)
                }.distinct().takeIf { queryTables ->
                    queryTables.isNotEmpty()
                } ?: throw PaginationError.InvalidOrderField(fieldName = order.field)
            } ?: queryTables

            // Retrieve the column from the target tables based on the field name.
            val key: String = generateCacheKey(context = queryTables, order = order)
            val column: Column<*> = getColumn(
                key = key,
                fieldName = order.field,
                targets = targetTables
            )

            // Apply the sorting order to the query based on the direction
            // specified in the Pageable.
            val sortOrder: SortOrder = when (order.direction) {
                Pageable.Direction.ASC -> SortOrder.ASC
                Pageable.Direction.DESC -> SortOrder.DESC
            }
            query.orderBy(column to sortOrder)
        }
    }

    /**
     * Attempts to retrieve a column, first from the cache, or of not found, try to resolve
     * it via reflection from the given list of table [targets] and cache it.
     *
     * @param key The cache key to retrieve/store the column reference.
     * @param fieldName The name of the field representing the column.
     * @param targets A list of tables to search for the column.
     * @return The found Column reference, or null if not found.
     */
    private fun getColumn(key: String, fieldName: String, targets: List<Table>): Column<*> {
        columnCache[key]?.let { column ->
            return column
        }

        val columns: List<Column<*>> = targets.asSequence().flatMap { table ->
            resolveTableColumn(table = table, fieldName = fieldName)
        }.distinct().toList()

        if (columns.isEmpty()) {
            throw PaginationError.InvalidOrderField(fieldName = fieldName)
        } else if (columns.size > 1) {
            val reason = "'$fieldName' found in: ${columns.joinToString { it.table.tableName }}"
            throw PaginationError.AmbiguousOrderField(fieldName = fieldName, reason = reason)
        }

        val column: Column<*> = columns.single()
        columnCache[key] = column
        return column
    }

    /**
     * Searches for a column in the given [table] by matching its property names
     * with the given [fieldName].
     *
     * @param table The table to search for the column.
     * @param fieldName The name of the field representing the column.
     * @return List of Column references from the table that match the field name.
     */
    private fun resolveTableColumn(table: Table, fieldName: String): List<Column<*>> {
        return table::class.memberProperties.filter { property ->
            // Look for a property in the table class that matches the field name and is a Column type.
            property.returnType.classifier == Column::class &&
                    property.name.equals(other = fieldName, ignoreCase = true)
        }.mapNotNull { property ->
            runCatching {
                // Attempt to retrieve the Column property from the table.
                tracer.debug("Column matched. ${table.tableName}::${property.name}.")
                return@runCatching property.getter.call(table) as? Column<*>
            }.onFailure { exception ->
                // Log the exception if the reflection call fails, as it may indicate a misconfiguration.
                tracer.error(message = "Failed to access column. ${table.tableName}::${property.name}", cause = exception)
            }.getOrNull()
        }
    }

    /**
     * Generates a unique cache key for column lookup by combining all table names involved in the query
     * with the specified sorting directives.
     *
     * If the sorting table name is not specified in the directives, it defaults to `null`, maintaining uniqueness.
     * This design allows caching the same column under two keys to optimize column retrieval,
     * as long as no ambiguity is detected (e.g., `query-tables=null.fieldName` or `query-tables=tableName.fieldName`).
     *
     * This approach prevents skipping essential ambiguity checks for fields that appear across different
     * tables in different queries.
     *
     * @param context A list of tables involved in the query.
     * @param order The sorting directive used to refine the key.
     * @return A string representing the cache key, uniquely identifying a column within the query context.
     */
    private fun generateCacheKey(context: List<Table>, order: Pageable.Sort): String {
        val tableNames: String = context.joinToString("::") { it.tableName.lowercase() }
        return "$tableNames=${order.table?.lowercase()}.${order.field.lowercase()}"
    }
}
