/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.domain.employment.model

import kcrud.core.database.schema.employment.types.EmploymentStatus
import kcrud.core.database.schema.employment.types.WorkModality
import kcrud.core.persistence.model.Period
import kcrud.domain.employment.errors.EmploymentError
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
) {
    /**
     * Example of a validation within a data class.
     * This is not a good practice, as it couples the data class with the validation logic.
     *
     * ```
     *      init {
     *          probationEndDate?.let { date ->
     *              require(date >= period.startDate) {
     *                  "Employment probation end date cannot be earlier than period start date."
     *              }
     *          }
     *      }
     * ```
     *
     * Such approach, other than an error message, does not allow to pass to the client
     * more contextual information about the error, such as the field name decoupled from
     * the message, and/or a more concrete error code which may reflect how the error happened,
     * for example whether creating or updating, and other details such as record IDs, etc.
     * For a better approach, see the usage of [EmploymentError.PeriodDatesMismatch].
     */
}
