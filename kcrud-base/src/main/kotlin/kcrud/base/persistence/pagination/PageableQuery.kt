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
 * @throws IllegalArgumentException If an invalid sort table or column is specified.
 */
public fun Query.paginate(pageable: Pageable?): Query {
    pageable?.let {
        QueryOrderingHelper.applyOrder(query = this, pageable = pageable)

        if (pageable.size > 0) {
            val startIndex: Int = pageable.page * pageable.size
            this.limit(n = pageable.size, offset = startIndex.toLong())
        }
    }

    return this
}

/**
 * Handles the determination and application of column-based ordering for database queries according
 * to the provided [Pageable] object.
 *
 * Reflection is used to dynamically resolve column references from field names, and employing caching
 * to optimize this process reducing the overhead of repetitive reflection.
 */
private object QueryOrderingHelper {
    private val tracer = Tracer<QueryOrderingHelper>()

    /**
     * Represents a key for the [columnCache].
     *
     * @param tableClass The class of the table containing the column.
     * @param fieldName The name of the field representing the column.
     */
    private data class CacheKey(val tableClass: KClass<out Table>, val fieldName: String)

    /**
     * Cache storing column references with table class and column name as the key.
     * Used to optimize the reflection process of finding columns.
     */
    private val columnCache: MutableMap<CacheKey, Column<*>> = ConcurrentHashMap()

    /**
     * Applies ordering to a query based on the provided Pageable object.
     * Orders the query according to the fields and directions specified in Pageable.
     *
     * @param query The query to apply ordering to.
     * @param pageable An optional Pageable object containing ordering information.
     * @throws IllegalArgumentException If an invalid sort table or column is specified.
     */
    fun applyOrder(query: Query, pageable: Pageable?) {
        pageable?.sort?.forEach { order ->

            // Filter query targets to find matching tables if a specific table name is provided.
            // If no matching tables are found, throw an IllegalArgumentException.
            // If no table is provided, all query targets are considered for column search.
            val targetTables: List<Table> = order.table?.let { tableName ->
                query.targets.filter { table ->
                    table.tableName.equals(other = tableName, ignoreCase = true)
                }.takeIf { queryTables -> queryTables.isNotEmpty() }
                    ?: throw IllegalArgumentException("Invalid sort table: $tableName")
            } ?: query.targets

            // Retrieve the column from the target tables based on the field name.
            val column: Column<*> = getColumn(targets = targetTables, fieldName = order.field)
                ?: throw IllegalArgumentException(
                    "Invalid sort column: '${order.field}'" +
                            (order.table?.let { table -> " in table '$table'" } ?: "")
                )

            // Apply the sorting order to the query based on the direction specified in the Pageable.
            val sortOrder: SortOrder = SortOrder.ASC.takeIf { order.direction == Pageable.Direction.ASC } ?: SortOrder.DESC
            query.orderBy(column to sortOrder)
        }
    }

    /**
     * Attempts to retrieve a column, first from the cache, or of not found, try to resolve
     * it via reflection from the given list of table [targets] and cache it.
     *
     * @param targets A list of tables to search for the column.
     * @param fieldName The name of the field representing the column.
     * @return The found Column reference, or null if not found.
     */
    private fun getColumn(targets: List<Table>, fieldName: String): Column<*>? {
        return targets.asSequence().mapNotNull { table ->
            val key = CacheKey(tableClass = table::class, fieldName = fieldName.lowercase())

            // Retrieve from cache, or resolve and cache if not found.
            return@mapNotNull columnCache[key] ?: resolveTableColumn(
                table = table,
                fieldName = fieldName
            )?.also { column ->
                columnCache[key] = column
            }
        }.firstOrNull()
    }

    /**
     * Searches for a column in the given [table] by matching its property names
     * with the given [fieldName].
     *
     * @param table The table to search for the column.
     * @param fieldName The name of the field representing the column.
     * @return The Column reference from the table, or null if not found.
     */
    private fun resolveTableColumn(table: Table, fieldName: String): Column<*>? {
        return table::class.memberProperties
            .firstOrNull { property ->
                // Look for a property in the table class that matches the field name and is a Column type.
                property.name.equals(fieldName, ignoreCase = true) &&
                        property.returnType.classifier == Column::class
            }?.let { property ->
                runCatching {
                    // Attempt to retrieve the Column property from the table.
                    tracer.debug("Column matched. ${table.tableName}::${property.name}.")
                    return property.getter.call(table) as? Column<*>
                }.onFailure { exception ->
                    // Log the exception if the reflection call fails, as it may indicate a misconfiguration.
                    tracer.error(message = "Failed to access column. ${table.tableName}::${property.name}", cause = exception)
                }.getOrNull()
            }
    }
}
