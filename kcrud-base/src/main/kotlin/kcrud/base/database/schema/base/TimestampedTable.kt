/*
 * Copyright (c) 2024-Present Perracodex. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.database.schema.base

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

/**
 * Base class for database tables holding entities with creation and modification timestamps.
 */
@Suppress("unused")
open class TimestampedTable(name: String) : Table(name = name) {
    /**
     * The timestamp when the record was created.
     */
    val createdAt = datetime(
        name = "created_at"
    ).defaultExpression(defaultValue = CurrentDateTime)

    /**
     * The timestamp when the record was last updated.
     */
    val updatedAt = datetime(
        name = "updated_at"
    ).defaultExpression(defaultValue = CurrentDateTime)
}
