/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.database.schema.base

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestampWithTimeZone
import org.jetbrains.exposed.sql.kotlin.datetime.timestampWithTimeZone
import java.time.OffsetDateTime

/**
 * Base class for database tables that automatically track the creation and last update timestamps of records.
 */
public abstract class TimestampedTable(name: String) : Table(name = name) {

    /** Timestamp for when the record was initially created. */
    public val createdAt: Column<OffsetDateTime> = timestampWithTimeZone(
        name = "created_at"
    ).defaultExpression(defaultValue = CurrentTimestampWithTimeZone)

    /** Timestamp for the most recent update to the record. */
    public val updatedAt: Column<OffsetDateTime> = timestampWithTimeZone(
        name = "updated_at"
    ).defaultExpression(defaultValue = CurrentTimestampWithTimeZone)
}
