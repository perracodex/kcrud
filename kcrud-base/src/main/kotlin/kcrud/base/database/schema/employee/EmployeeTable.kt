/*
 * Copyright (c) 2024-Present Perraco. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.database.schema.employee

import kcrud.base.database.schema.employee.types.Honorific
import kcrud.base.database.schema.employee.types.MaritalStatus
import kcrud.base.persistence.utils.enumById
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

/**
 * Database table definition for employees.
 */
object EmployeeTable : Table(name = "employee") {
    /**
     * The unique id of the employee record.
     */
    val id = uuid(
        name = "employee_id"
    ).autoGenerate()

    /**
     * The employee's first name.
     */
    val firstName = varchar(
        name = "first_name",
        length = 64
    )

    /**
     * The employee's last name.
     */
    val lastName = varchar(
        name = "last_name",
        length = 64
    )

    /**
     * The employee's date of birth.
     */
    val dob = date(
        name = "dob"
    )

    /**
     * The employee's [MaritalStatus].
     *
     * Example of an enum that is stored as a string in the database.
     */
    val maritalStatus = enumerationByName(
        name = "marital_status",
        length = 64,
        klass = MaritalStatus::class
    )

    /**
     * The employee's [Honorific].
     *
     * Example of an enum that is stored as an integer in the database.
     */
    val honorific = enumById(
        name = "honorific",
        fromId = Honorific::fromId
    )

    /**
     * The timestamp when the record was created.
     */
    val createdAt = datetime(
        name = "created_at"
    ).defaultExpression(CurrentDateTime)

    /**
     * The timestamp when the record was last updated.
     */
    val updatedAt = datetime(
        name = "updated_at"
    ).defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(
        firstColumn = id,
        name = "pk_employee_id"
    )

    init {
        index(
            customIndexName = "ix_employee__first_name",
            isUnique = false,
            columns = arrayOf(firstName)
        )

        index(
            customIndexName = "ix_employee__last_name",
            isUnique = false,
            columns = arrayOf(lastName)
        )
    }
}
