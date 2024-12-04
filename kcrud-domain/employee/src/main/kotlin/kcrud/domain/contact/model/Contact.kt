/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.domain.contact.model

import io.perracodex.exposed.pagination.MapModel
import kcrud.core.plugins.Uuid
import kcrud.database.model.Meta
import kcrud.database.schema.contact.ContactTable
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
public data class Contact(
    val id: Uuid,
    val email: String,
    val phone: String,
    val meta: Meta
) {
    internal companion object : MapModel<Contact> {
        override fun from(row: ResultRow): Contact {
            return Contact(
                id = row[ContactTable.id],
                email = row[ContactTable.email],
                phone = row[ContactTable.phone],
                meta = Meta.from(row = row, table = ContactTable)
            )
        }
    }
}
