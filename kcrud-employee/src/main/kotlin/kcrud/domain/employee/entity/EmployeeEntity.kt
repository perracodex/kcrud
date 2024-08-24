/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.domain.employee.entity

import kcrud.base.database.schema.contact.ContactTable
import kcrud.base.database.schema.employee.EmployeeTable
import kcrud.base.database.schema.employee.types.Honorific
import kcrud.base.database.schema.employee.types.MaritalStatus
import kcrud.base.persistence.entity.Meta
import kcrud.base.persistence.serializers.UuidS
import kcrud.base.utils.DateTimeUtils
import kcrud.base.utils.KLocalDate
import kcrud.domain.contact.entity.ContactEntity
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.ResultRow
import kotlin.uuid.toKotlinUuid

/**
 * Represents the entity for an employee.
 *
 * @property id The employee's id.
 * @property firstName The first name of the employee.
 * @property lastName The last name of the employee.
 * @property fullName The full name of the employee, computed as "lastName, firstName".
 * @property dob The date of birth of the employee.
 * @property age The age of the employee, computed from [dob].
 * @property maritalStatus The marital status of the employee.
 * @property honorific The honorific or title of the employee.
 * @property contact Optional contact details of the employee.
 * @property meta The metadata of the record.
 */
@Serializable
data class EmployeeEntity(
    val id: UuidS,
    val firstName: String,
    val lastName: String,
    val fullName: String,
    val dob: KLocalDate,
    val age: Int,
    val maritalStatus: MaritalStatus,
    val honorific: Honorific,
    val contact: ContactEntity?,
    val meta: Meta
) {
    companion object {
        /**
         * Maps a [ResultRow] to a [EmployeeEntity] instance.
         *
         * @param row The [ResultRow] to map.
         * @return The mapped [EmployeeEntity] instance.
         */
        fun from(row: ResultRow): EmployeeEntity {
            val contact: ContactEntity? = row.getOrNull(ContactTable.id)?.let {
                ContactEntity.from(row = row)
            }

            val dob: KLocalDate = row[EmployeeTable.dob]
            val firstName: String = row[EmployeeTable.firstName]
            val lastName: String = row[EmployeeTable.lastName]

            return EmployeeEntity(
                id = row[EmployeeTable.id].toKotlinUuid(),
                firstName = firstName,
                lastName = lastName,
                fullName = "$lastName, $firstName",
                dob = dob,
                age = DateTimeUtils.age(dob = dob),
                maritalStatus = row[EmployeeTable.maritalStatus],
                honorific = row[EmployeeTable.honorific],
                contact = contact,
                meta = Meta.from(row = row, table = EmployeeTable)
            )
        }
    }
}
