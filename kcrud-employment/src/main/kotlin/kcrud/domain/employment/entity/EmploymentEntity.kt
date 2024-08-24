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
import kcrud.domain.employee.entity.EmployeeEntity
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.ResultRow
import kotlin.uuid.toKotlinUuid

/**
 * Represents the entity for an employment.
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
public data class EmploymentEntity(
    val id: UuidS,
    val period: Period,
    val status: EmploymentStatus,
    val probationEndDate: KLocalDate?,
    val workModality: WorkModality,
    val employee: EmployeeEntity,
    val meta: Meta
) {
    internal companion object {
        /**
         * Maps a [ResultRow] to a [EmploymentEntity] instance.
         *
         * @param row The [ResultRow] to map.
         * @return The mapped [EmploymentEntity] instance.
         */
        fun from(row: ResultRow): EmploymentEntity {
            return EmploymentEntity(
                id = row[EmploymentTable.id].toKotlinUuid(),
                period = Period.from(row = row, table = EmploymentTable),
                status = row[EmploymentTable.status],
                probationEndDate = row[EmploymentTable.probationEndDate],
                workModality = row[EmploymentTable.workModality],
                employee = EmployeeEntity.from(row = row),
                meta = Meta.from(row = row, table = EmploymentTable)
            )
        }
    }
}
