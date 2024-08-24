/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.persistence.entity

import kcrud.base.utils.KLocalDate
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table

/**
 * Represents a time period.
 *
 * @property isActive Whether the period is currently active.
 * @property startDate The start date of the period.
 * @property endDate The end date of the period.
 * @property comments Optional notes or comments.
 */
@Serializable
public data class Period(
    val isActive: Boolean,
    val startDate: KLocalDate,
    val endDate: KLocalDate?,
    val comments: String?
) {
    public companion object {
        /**
         * Maps a [ResultRow] to a [Period] instance.
         *
         * @param row The [ResultRow] to map.
         * @param table The [Table] that the [ResultRow] is from.
         * @return The mapped [Period] instance.
         */
        public fun from(row: ResultRow, table: Table): Period {
            val isActiveColumn: Column<*> = table.columns.single { it.name == "is_active" }
            val startDateColumn: Column<*> = table.columns.single { it.name == "start_date" }
            val endDateColumn: Column<*> = table.columns.single { it.name == "end_date" }
            val commentsColumn: Column<*> = table.columns.single { it.name == "comments" }

            return Period(
                isActive = row[isActiveColumn] as Boolean,
                startDate = row[startDateColumn] as KLocalDate,
                endDate = row[endDateColumn] as KLocalDate?,
                comments = row[commentsColumn] as String?
            )
        }
    }
}
