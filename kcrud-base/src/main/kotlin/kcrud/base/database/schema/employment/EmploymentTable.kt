/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.database.schema.employment

import kcrud.base.database.schema.base.PeriodTable
import kcrud.base.database.schema.employee.EmployeeTable
import kcrud.base.database.schema.employment.types.EmploymentStatus
import kcrud.base.database.schema.employment.types.WorkModality
import kcrud.base.persistence.utils.enumerationById
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
public object EmploymentTable : PeriodTable(name = "employment") {
    /**
     * The unique id of the employment record.
     */
    public val id: Column<UUID> = uuid(
        name = "employment_id"
    ).autoGenerate()

    /**
     * The id of the employee to which the employment belongs.
     */
    public val employeeId: Column<UUID> = uuid(
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
    public val status: Column<EmploymentStatus> = enumerationById(
        name = "status_id",
        fromId = EmploymentStatus::fromId
    )

    /**
     * The date the employment started.
     */
    public val probationEndDate: Column<KLocalDate?> = date(
        name = "probation_end_date"
    ).nullable()

    /**
     * The [WorkModality] of the employment.
     */
    public val workModality: Column<WorkModality> = enumerationById(
        name = "work_modality_id",
        fromId = WorkModality::fromId
    )

    /**
     * Optional comments or notes for the employment.
     */
    public val comments: Column<String?> = varchar(
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
