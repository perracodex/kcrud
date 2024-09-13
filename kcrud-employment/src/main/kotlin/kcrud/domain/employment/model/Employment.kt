/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.domain.employment.model

import kcrud.base.database.schema.employment.EmploymentTable
import kcrud.base.database.schema.employment.types.EmploymentStatus
import kcrud.base.database.schema.employment.types.WorkModality
import kcrud.base.persistence.model.Meta
import kcrud.base.persistence.model.Period
import kcrud.base.persistence.serializers.SUuid
import kcrud.base.utils.KLocalDate
import kcrud.domain.employee.model.Employee
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
 * @property employee The employment's [Employee].
 * @property meta The metadata of the record.
 */
@Serializable
public data class Employment(
    val id: SUuid,
    val period: Period,
    val status: EmploymentStatus,
    val probationEndDate: KLocalDate?,
    val workModality: WorkModality,
    val employee: Employee,
    val meta: Meta
) {
    internal companion object {
        /**
         * Maps a [ResultRow] to a [Employment] instance.
         *
         * @param row The [ResultRow] to map.
         * @return The mapped [Employment] instance.
         */
        fun from(row: ResultRow): Employment {
            return Employment(
                id = row[EmploymentTable.id],
                period = Period.from(row = row, table = EmploymentTable),
                status = row[EmploymentTable.status],
                probationEndDate = row[EmploymentTable.probationEndDate],
                workModality = row[EmploymentTable.workModality],
                employee = Employee.from(row = row),
                meta = Meta.from(row = row, table = EmploymentTable)
            )
        }
    }
}
