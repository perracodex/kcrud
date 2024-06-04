/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.database.schema.employee

import kcrud.base.database.schema.base.TimestampedTable
import kcrud.base.database.schema.employee.types.Honorific
import kcrud.base.database.schema.employee.types.MaritalStatus
import kcrud.base.persistence.utils.enumById
import kcrud.base.utils.KLocalDate
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.date
import java.util.*

/**
 * Database table definition for employees.
 */
object EmployeeTable : TimestampedTable(name = "employee") {
    /**
     * The unique id of the employee record.
     */
    val id: Column<UUID> = uuid(
        name = "employee_id"
    ).autoGenerate()

    /**
     * The employee's first name.
     */
    val firstName: Column<String> = varchar(
        name = "first_name",
        length = 64
    )

    /**
     * The employee's last name.
     */
    val lastName: Column<String> = varchar(
        name = "last_name",
        length = 64
    )

    /**
     * The employee's date of birth.
     */
    val dob: Column<KLocalDate> = date(
        name = "dob"
    )

    /**
     * The employee's [MaritalStatus].
     *
     * Example of an enum that is stored as a string in the database.
     */
    val maritalStatus: Column<MaritalStatus> = enumerationByName(
        name = "marital_status",
        length = 64,
        klass = MaritalStatus::class
    )

    /**
     * The employee's [Honorific].
     *
     * Example of an enum that is stored as an integer in the database.
     */
    val honorific: Column<Honorific> = enumById(
        name = "honorific",
        fromId = Honorific::fromId
    )

    override val primaryKey: Table.PrimaryKey = PrimaryKey(
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
