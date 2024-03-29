/*
 * Copyright (c) 2024-Present Perraco. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.domain.employee.entity

import kcrud.base.database.schema.contact.ContactTable
import kcrud.base.database.schema.employee.EmployeeTable
import kcrud.base.database.schema.employee.types.Honorific
import kcrud.base.database.schema.employee.types.MaritalStatus
import kcrud.base.persistence.entity.Meta
import kcrud.base.persistence.serializers.SUUID
import kcrud.base.utils.DateTimeUtils
import kcrud.base.utils.KLocalDate
import kcrud.domain.contact.entity.ContactEntity
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.ResultRow

/**
 * Represents the entity for an employee.
 *
 * @property id The employee's id.
 * @property firstName The first name of the employee.
 * @property lastName The last name of the employee.
 * @property dob The date of birth of the employee.
 * @property maritalStatus The marital status of the employee.
 * @property honorific The honorific or title of the employee.
 * @property contact Optional contact details of the employee.
 * @property meta The metadata of the record.
 */
@Serializable
data class EmployeeEntity(
    val id: SUUID,
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
        fun from(row: ResultRow): EmployeeEntity {
            val contact: ContactEntity? = row.getOrNull(ContactTable.id)?.let {
                ContactEntity.from(row = row)
            }

            val dob: KLocalDate = row[EmployeeTable.dob]
            val firstName: String = row[EmployeeTable.firstName]
            val lastName: String = row[EmployeeTable.lastName]

            return EmployeeEntity(
                id = row[EmployeeTable.id],
                firstName = firstName,
                lastName = lastName,
                fullName = "$lastName, $firstName",
                dob = dob,
                age = DateTimeUtils.age(dob = dob),
                maritalStatus = row[EmployeeTable.maritalStatus],
                honorific = row[EmployeeTable.honorific],
                contact = contact,
                meta = Meta.toEntity(row = row, table = EmployeeTable)
            )
        }
    }
}
