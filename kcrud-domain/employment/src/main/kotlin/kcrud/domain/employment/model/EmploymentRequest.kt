/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.domain.employment.model

import kcrud.database.model.Period
import kcrud.database.schema.employment.type.EmploymentStatus
import kcrud.database.schema.employment.type.WorkModality
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/**
 * Represents the request to create/update an employment.
 *
 * @property period The employment's period details.
 * @property status The [EmploymentStatus] to set.
 * @property probationEndDate Optional employment's probation end date.
 * @property workModality The employment's [WorkModality] to set.
 * @property sensitiveData Optional sensitive data. Demonstrates encrypted columns.
 */
@Serializable
public data class EmploymentRequest(
    val period: Period,
    val status: EmploymentStatus,
    val probationEndDate: LocalDate? = null,
    val workModality: WorkModality,
    val sensitiveData: String? = null
)
