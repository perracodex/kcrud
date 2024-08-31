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
 * Base class for database tables holding entities with creation and modification timestamps.
 */
public open class TimestampedTable(name: String) : Table(name = name) {

    /** The timestamp when the record was created. */
    public val createdAt: Column<OffsetDateTime> = timestampWithTimeZone(
        name = "created_at"
    ).defaultExpression(defaultValue = CurrentTimestampWithTimeZone)

    /** The timestamp when the record was last updated.  */
    public val updatedAt: Column<OffsetDateTime> = timestampWithTimeZone(
        name = "updated_at"
    ).defaultExpression(defaultValue = CurrentTimestampWithTimeZone)
}
