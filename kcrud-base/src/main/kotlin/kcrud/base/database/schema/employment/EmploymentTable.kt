/*
 * Copyright (c) 2024-Present Perraco. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.database.schema.employment

import kcrud.base.database.schema.base.PeriodTable
import kcrud.base.database.schema.employee.EmployeeTable
import kcrud.base.database.schema.employment.types.EmploymentStatus
import kcrud.base.database.schema.employment.types.WorkModality
import kcrud.base.persistence.utils.enumById
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.date

/**
 * Database table definition for employments.
 *
 * An employee may have multiple employments, which indicates re-hiring.
 */
object EmploymentTable : PeriodTable(name = "employment") {
    /**
     * The unique id of the employment record.
     */
    val id = uuid(
        name = "employment_id"
    ).autoGenerate()

    /**
     * The id of the employee to which the employment belongs.
     */
    val employeeId = uuid(
        name = "employee_id"
    ).references(
        fkName = "fk_employment__employee_id",
        ref = EmployeeTable.id,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.RESTRICT
    )

    /**
     * The status of the employment.
     */
    val status = enumById(
        name = "status",
        fromId = EmploymentStatus::fromId
    )

    /**
     * The date the employment started.
     */
    val probationEndDate = date(
        name = "probation_end_date"
    ).nullable()

    /**
     * The [WorkModality] of the employment.
     */
    val workModality = enumById(
        name = "work_modality",
        fromId = WorkModality::fromId
    )

    /**
     * Optional comments or notes for the employment.
     */
    val comments = varchar(
        name = "comments",
        length = 512
    ).nullable()

    override val primaryKey = PrimaryKey(
        firstColumn = id,
        name = "pk_employment_id"
    )

    init {
        index(
            customIndexName = "ix_employment__employee_id",
            isUnique = false,
            columns = arrayOf(employeeId)
        )
    }
}
