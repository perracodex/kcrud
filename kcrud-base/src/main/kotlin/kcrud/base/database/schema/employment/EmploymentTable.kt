/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.database.schema.employment

import kcrud.base.database.columns.autoGenerate
import kcrud.base.database.columns.enumerationById
import kcrud.base.database.columns.kotlinUuid
import kcrud.base.database.columns.references
import kcrud.base.database.schema.base.PeriodTable
import kcrud.base.database.schema.employee.EmployeeTable
import kcrud.base.database.schema.employment.types.EmploymentStatus
import kcrud.base.database.schema.employment.types.WorkModality
import kcrud.base.utils.KLocalDate
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.date
import kotlin.uuid.Uuid

/**
 * Database table definition for employments.
 *
 * An employee may have multiple employments, which indicates re-hiring.
 */
public object EmploymentTable : PeriodTable(name = "employment") {
    /**
     * The unique id of the employment record.
     */
    public val id: Column<Uuid> = kotlinUuid(
        name = "employment_id"
    ).autoGenerate()

    /**
     * The id of the employee to which the employment belongs.
     */
    public val employeeId: Column<Uuid> = kotlinUuid(
        name = "employee_id"
    ).references(
        ref = EmployeeTable.id,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.RESTRICT,
        fkName = "fk_employment__employee_id"
    )

    /**
     * The status of the employment.
     */
    public val status: Column<EmploymentStatus> = enumerationById(
        name = "status_id"
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
        name = "work_modality_id"
    )

    /**
     * The table's primary key.
     */
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
