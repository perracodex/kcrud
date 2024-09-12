/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.domain.contact.model

import kcrud.base.database.schema.contact.ContactTable
import kcrud.base.persistence.model.Meta
import kcrud.base.persistence.serializers.UuidS
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.ResultRow

/**
 * Represents a concrete contact detail for an employee.
 *
 * @property id The contact's id.
 * @property email The contact's email.
 * @property phone The contact's phone number.
 * @property meta The metadata of the record.
 */
@Serializable
public data class ContactDto(
    val id: UuidS,
    val email: String,
    val phone: String,
    val meta: Meta
) {
    internal companion object {
        /**
         * Maps a [ResultRow] to a [ContactDto] instance.
         *
         * @param row The [ResultRow] to map.
         * @return The mapped [ContactDto] instance.
         */
        fun from(row: ResultRow): ContactDto {
            return ContactDto(
                id = row[ContactTable.id],
                email = row[ContactTable.email],
                phone = row[ContactTable.phone],
                meta = Meta.from(row = row, table = ContactTable)
            )
        }
    }
}
