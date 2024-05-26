/*
 * Copyright (c) 2024-Present Perracodex. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.database.schema.base

import kcrud.base.utils.KLocalDate
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.kotlin.datetime.date

/**
 * Base class for database tables holding entities that require period tracking.
 */
open class PeriodTable(name: String) : TimestampedTable(name = name) {

    /**
     * Whether the employment is active or not.
     */
    val isActive: Column<Boolean> = bool(
        name = "is_active"
    )

    /**
     * The date the employment started.
     */
    val startDate: Column<KLocalDate> = date(
        name = "start_date"
    )

    /**
     * The date the employment ended.
     */
    val endDate: Column<KLocalDate?> = date(
        name = "end_date"
    ).nullable()
}
