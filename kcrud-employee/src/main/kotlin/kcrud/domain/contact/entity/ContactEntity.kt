/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.domain.contact.entity

import kcrud.base.database.schema.contact.ContactTable
import kcrud.base.persistence.entity.Meta
import kcrud.base.persistence.serializers.SUUID
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.ResultRow

/**
 * Represents the entity for an employee's contact details.
 *
 * @property id The contact's id.
 * @property email The contact's email.
 * @property phone The contact's phone.
 * @property meta The metadata of the record.
 */
@Serializable
data class ContactEntity(
    val id: SUUID,
    val email: String,
    val phone: String,
    val meta: Meta
) {
    companion object {
        /**
         * Maps a [ResultRow] to a [ContactEntity] instance.
         *
         * @param row The [ResultRow] to map.
         * @return The mapped [ContactEntity] instance.
         */
        fun from(row: ResultRow): ContactEntity {
            return ContactEntity(
                id = row[ContactTable.id],
                email = row[ContactTable.email],
                phone = row[ContactTable.phone],
                meta = Meta.toEntity(row = row, table = ContactTable)
            )
        }
    }
}
