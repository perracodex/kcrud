/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.core.persistence.model

import kcrud.core.database.schema.base.TimestampedTable
import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.ResultRow

/**
 * Represents the metadata of a record.
 *
 * @property createdAt The timestamp when the record was created, in UTC.
 * @property updatedAt The timestamp when the record was last updated, in UTC.
 */
@Serializable
public data class Meta(
    val createdAt: Instant,
    val updatedAt: Instant
) {
    public companion object {
        /**
         * Maps a [ResultRow] to a [Meta] instance, converting timestamps to UTC.
         * This conversion ensures that the timestamps are timezone-agnostic
         * and can be consistently interpreted in any geographical location.
         *
         * @param row The [ResultRow] to map.
         * @param table The [TimestampedTable] from which the [ResultRow] was obtained.
         * @return The mapped [Meta] instance with timestamps in UTC.
         */
        public fun from(row: ResultRow, table: TimestampedTable): Meta {
            return Meta(
                createdAt = row[table.createdAt].toInstant().toKotlinInstant(),
                updatedAt = row[table.updatedAt].toInstant().toKotlinInstant()
            )
        }
    }
}
