/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.domain.employment.entity

import kcrud.base.database.schema.employment.EmploymentTable
import kcrud.base.database.schema.employment.types.EmploymentStatus
import kcrud.base.database.schema.employment.types.WorkModality
import kcrud.base.persistence.entity.Meta
import kcrud.base.persistence.entity.Period
import kcrud.base.persistence.serializers.UuidS
import kcrud.base.utils.KLocalDate
import kcrud.domain.employee.entity.EmployeeDto
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.ResultRow

/**
 * Represents a concrete employment.
 *
 * @property id The employment's id.
 * @property period The employment's period details.
 * @property status The employment's current status.
 * @property probationEndDate Optional employment's probation end date.
 * @property workModality The employment's work modality.
 * @property employee The employment's employee.
 * @property meta The metadata of the record.
 */
@Serializable
public data class EmploymentDto(
    val id: UuidS,
    val period: Period,
    val status: EmploymentStatus,
    val probationEndDate: KLocalDate?,
    val workModality: WorkModality,
    val employee: EmployeeDto,
    val meta: Meta
) {
    internal companion object {
        /**
         * Maps a [ResultRow] to a [EmploymentDto] instance.
         *
         * @param row The [ResultRow] to map.
         * @return The mapped [EmploymentDto] instance.
         */
        fun from(row: ResultRow): EmploymentDto {
            return EmploymentDto(
                id = row[EmploymentTable.id],
                period = Period.from(row = row, table = EmploymentTable),
                status = row[EmploymentTable.status],
                probationEndDate = row[EmploymentTable.probationEndDate],
                workModality = row[EmploymentTable.workModality],
                employee = EmployeeDto.from(row = row),
                meta = Meta.from(row = row, table = EmploymentTable)
            )
        }
    }
}
