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
import kcrud.base.utils.KLocalDate
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.date
import java.util.*

/**
 * Database table definition for employments.
 *
 * An employee may have multiple employments, which indicates re-hiring.
 */
object EmploymentTable : PeriodTable(name = "employment") {
    /**
     * The unique id of the employment record.
     */
    val id: Column<UUID> = uuid(
        name = "employment_id"
    ).autoGenerate()

    /**
     * The id of the employee to which the employment belongs.
     */
    val employeeId: Column<UUID> = uuid(
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
    val status: Column<EmploymentStatus> = enumById(
        name = "status",
        fromId = EmploymentStatus::fromId
    )

    /**
     * The date the employment started.
     */
    val probationEndDate: Column<KLocalDate?> = date(
        name = "probation_end_date"
    ).nullable()

    /**
     * The [WorkModality] of the employment.
     */
    val workModality: Column<WorkModality> = enumById(
        name = "work_modality",
        fromId = WorkModality::fromId
    )

    /**
     * Optional comments or notes for the employment.
     */
    val comments: Column<String?> = varchar(
        name = "comments",
        length = 512
    ).nullable()

    override val primaryKey: Table.PrimaryKey = PrimaryKey(
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
