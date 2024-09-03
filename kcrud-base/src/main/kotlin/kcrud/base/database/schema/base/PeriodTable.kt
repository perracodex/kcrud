/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.database.schema.base

import kcrud.base.utils.KLocalDate
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.kotlin.datetime.date

/**
 * Base class for database tables that manage temporal aspects of entities. Designed to be extended,
 * this class provides foundational columns for tracking the active status and temporal boundaries
 * of periods associated with various entities.
 * This structure supports applications needing to capture historical data or manage ongoing events.
 */
public open class PeriodTable(name: String) : TimestampedTable(name = name) {

    /** Whether the period is currently active. */
    public val isActive: Column<Boolean> = bool(
        name = "is_active"
    )

    /** The date when the period started. */
    public val startDate: Column<KLocalDate> = date(
        name = "start_date"
    )

    /** Optional date when the period ended; null if ongoing. */
    public val endDate: Column<KLocalDate?> = date(
        name = "end_date"
    ).nullable()

    /** Optional notes for additional context or details about the period. */
    public val comments: Column<String?> = varchar(
        name = "comments",
        length = 512
    ).nullable()
}
