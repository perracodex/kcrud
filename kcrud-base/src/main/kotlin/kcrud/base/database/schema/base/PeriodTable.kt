/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.database.schema.base

import kcrud.base.utils.KLocalDate
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.kotlin.datetime.date

/**
 * Base class for database tables holding entities that require period tracking.
 */
open class PeriodTable(name: String) : TimestampedTable(name = name) {

    /** Whether the period record is active or not. */
    val isActive: Column<Boolean> = bool(
        name = "is_active"
    )

    /** The date the period started. */
    val startDate: Column<KLocalDate> = date(
        name = "start_date"
    )

    /** The date the period ended. */
    val endDate: Column<KLocalDate?> = date(
        name = "end_date"
    ).nullable()
}
