/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.core.database.schema.employee

import kcrud.core.database.column.autoGenerate
import kcrud.core.database.column.enumerationById
import kcrud.core.database.column.kotlinUuid
import kcrud.core.database.column.validVarchar
import kcrud.core.database.schema.base.TimestampedTable
import kcrud.core.database.schema.employee.type.Honorific
import kcrud.core.database.schema.employee.type.MaritalStatus
import kcrud.core.error.validator.EmailValidator
import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.sql.Column
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
    ).index(
        customIndexName = "ix_employee__first_name"
    )

    /**
     * The employee's last name.
     */
    public val lastName: Column<String> = varchar(
        name = "last_name",
        length = 64
    ).index(
        customIndexName = "ix_employee__last_name"
    )

    /**
     * The employee's unique work email.
     */
    public val workEmail: Column<String> = validVarchar(
        name = "work_email",
        length = 128,
        validator = EmailValidator
    ).uniqueIndex(
        customIndexName = "uq_employee__work_email"
    )

    /**
     * The employee's date of birth.
     */
    public val dob: Column<LocalDate> = date(
        name = "dob"
    )

    /**
     * The employee's [MaritalStatus].
     *
     * Example of an enum that is stored as a string in the database.
     */
    public val maritalStatus: Column<MaritalStatus> = enumerationById(
        name = "marital_status",
        entries = MaritalStatus.entries
    )

    /**
     * The employee's [Honorific].
     *
     * Example of an enum that is stored as an integer in the database.
     */
    public val honorific: Column<Honorific> = enumerationById(
        name = "honorific_id",
        entries = Honorific.entries
    )

    /**
     * The table's primary key.
     */
    override val primaryKey: PrimaryKey = PrimaryKey(
        firstColumn = id,
        name = "pk_employee_id"
    )
}
