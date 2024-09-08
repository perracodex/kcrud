/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.database.schema.employee

import kcrud.base.database.columns.autoGenerate
import kcrud.base.database.columns.kotlinUuid
import kcrud.base.database.schema.base.TimestampedTable
import kcrud.base.database.schema.employee.types.Honorific
import kcrud.base.database.schema.employee.types.MaritalStatus
import kcrud.base.persistence.utils.enumerationById
import kcrud.base.persistence.utils.getEnumById
import kcrud.base.utils.KLocalDate
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.date
import kotlin.uuid.Uuid

/**
 * Database table definition for employees.
 */
public object EmployeeTable : TimestampedTable(name = "employee") {
    /**
     * The unique id of the employee record.
     */
    public val id: Column<Uuid> = kotlinUuid(
        name = "employee_id"
    ).autoGenerate()

    /**
     * The employee's first name.
     */
    public val firstName: Column<String> = varchar(
        name = "first_name",
        length = 64
    )

    /**
     * The employee's last name.
     */
    public val lastName: Column<String> = varchar(
        name = "last_name",
        length = 64
    )

    /**
     * The employee's date of birth.
     */
    public val dob: Column<KLocalDate> = date(
        name = "dob"
    )

    /**
     * The employee's [MaritalStatus].
     *
     * Example of an enum that is stored as a string in the database.
     */
    public val maritalStatus: Column<MaritalStatus> = enumerationById(
        name = "marital_status",
        fromId = ::getEnumById
    )

    /**
     * The employee's [Honorific].
     *
     * Example of an enum that is stored as an integer in the database.
     */
    public val honorific: Column<Honorific> = enumerationById(
        name = "honorific_id",
        fromId = ::getEnumById
    )

    /**
     * The table's primary key.
     */
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
