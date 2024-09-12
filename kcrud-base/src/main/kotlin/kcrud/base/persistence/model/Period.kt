/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.persistence.model

import kcrud.base.database.schema.base.PeriodTable
import kcrud.base.utils.KLocalDate
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.ResultRow

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
         * @param table The [PeriodTable] that the [ResultRow] is from.
         * @return The mapped [Period] instance.
         */
        public fun from(row: ResultRow, table: PeriodTable): Period {
            return Period(
                isActive = row[table.isActive],
                startDate = row[table.startDate],
                endDate = row[table.endDate],
                comments = row[table.comments]
            )
        }
    }
}
