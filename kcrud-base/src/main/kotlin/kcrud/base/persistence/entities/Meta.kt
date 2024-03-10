/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.persistence.entities

import kcrud.base.infrastructure.utils.KLocalDateTime
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table

/**
 * Represents the metadata of a record.
 *
 * @property createdAt The timestamp when the record was created.
 * @property updatedAt The timestamp when the record was last updated.
 */
@Serializable
data class Meta(
    val createdAt: KLocalDateTime,
    val updatedAt: KLocalDateTime
) {
    companion object {
        fun toEntity(row: ResultRow, table: Table): Meta {
            val createdAt: Column<*> = table.columns.single { it.name == "created_at" }
            val updatedAt: Column<*> = table.columns.single { it.name == "updated_at" }

            return Meta(
                createdAt = row[createdAt] as KLocalDateTime,
                updatedAt = row[updatedAt] as KLocalDateTime
            )
        }
    }
}
