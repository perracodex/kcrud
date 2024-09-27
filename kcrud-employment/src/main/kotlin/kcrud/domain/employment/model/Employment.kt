/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.domain.employment.model

import io.perracodex.exposed.pagination.IModelTransform
import kcrud.core.database.schema.employment.EmploymentTable
import kcrud.core.database.schema.employment.types.EmploymentStatus
import kcrud.core.database.schema.employment.types.WorkModality
import kcrud.core.persistence.model.Meta
import kcrud.core.persistence.model.Period
import kcrud.core.plugins.Uuid
import kcrud.domain.employee.model.Employee
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.ResultRow

/**
 * Represents a concrete employment.
 *
 * @property id The employment's id.
 * @property period The employment's period details.
 * @property status The current [EmploymentStatus].
 * @property probationEndDate Optional employment's probation end date.
 * @property workModality The employment's [WorkModality].
 * @property sensitiveData Optional sensitive data. Demonstrates encrypted columns.
 * @property employee The employment's [Employee].
 * @property meta The metadata of the record.
 */
@Serializable
public data class Employment(
    val id: Uuid,
    val period: Period,
    val status: EmploymentStatus,
    val probationEndDate: LocalDate?,
    val workModality: WorkModality,
    val sensitiveData: String?,
    val employee: Employee,
    val meta: Meta
) {
    internal companion object : IModelTransform<Employment> {
        override fun from(row: ResultRow): Employment {
            return Employment(
                id = row[EmploymentTable.id],
                period = Period.from(row = row, table = EmploymentTable),
                status = row[EmploymentTable.status],
                probationEndDate = row[EmploymentTable.probationEndDate],
                workModality = row[EmploymentTable.workModality],
                sensitiveData = row[EmploymentTable.sensitiveData],
                employee = Employee.from(row = row),
                meta = Meta.from(row = row, table = EmploymentTable)
            )
        }
    }
}
