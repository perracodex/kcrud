/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.domain.employee.model

import io.perracodex.exposed.pagination.IModelTransform
import kcrud.core.database.schema.contact.ContactTable
import kcrud.core.database.schema.employee.EmployeeTable
import kcrud.core.database.schema.employee.types.Honorific
import kcrud.core.database.schema.employee.types.MaritalStatus
import kcrud.core.persistence.model.Meta
import kcrud.core.plugins.Uuid
import kcrud.core.utils.DateTimeUtils.age
import kcrud.domain.contact.model.Contact
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.ResultRow

/**
 * Represents a concrete employee.
 *
 * @property id The employee's id.
 * @property firstName The first name of the employee.
 * @property lastName The last name of the employee.
 * @property fullName The full name of the employee, computed as "lastName, firstName".
 * @property workEmail The unique work email of the employee.
 * @property dob The date of birth of the employee.
 * @property age The age of the employee, computed from [dob].
 * @property maritalStatus The [MaritalStatus] of the employee.
 * @property honorific The [Honorific] or title of the employee.
 * @property contact Optional [Contact] details of the employee.
 * @property meta The metadata of the record.
 */
@Serializable
public data class Employee(
    val id: Uuid,
    val firstName: String,
    val lastName: String,
    val fullName: String,
    val workEmail: String,
    val dob: LocalDate,
    val age: Int,
    val maritalStatus: MaritalStatus,
    val honorific: Honorific,
    val contact: Contact?,
    val meta: Meta
) {
    public companion object : IModelTransform<Employee> {
        public override fun from(row: ResultRow): Employee {
            val contact: Contact? = row.getOrNull(ContactTable.id)?.let {
                Contact.from(row = row)
            }

            val dob: LocalDate = row[EmployeeTable.dob]
            val firstName: String = row[EmployeeTable.firstName]
            val lastName: String = row[EmployeeTable.lastName]

            return Employee(
                id = row[EmployeeTable.id],
                firstName = firstName,
                lastName = lastName,
                fullName = "$lastName, $firstName",
                workEmail = row[EmployeeTable.workEmail],
                dob = dob,
                age = dob.age(),
                maritalStatus = row[EmployeeTable.maritalStatus],
                honorific = row[EmployeeTable.honorific],
                contact = contact,
                meta = Meta.from(row = row, table = EmployeeTable)
            )
        }
    }
}
