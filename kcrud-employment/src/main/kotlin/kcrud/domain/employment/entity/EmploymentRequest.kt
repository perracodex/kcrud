/*
 * Copyright (c) 2023-Present Perracodex. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.domain.employment.entity

import kcrud.base.database.schema.employment.types.EmploymentStatus
import kcrud.base.database.schema.employment.types.WorkModality
import kcrud.base.persistence.entity.Period
import kcrud.base.utils.KLocalDate
import kcrud.domain.employment.errors.EmploymentError
import kotlinx.serialization.Serializable

/**
 * Represents the request to create/update an employment.
 *
 * @property period The employment's period details.
 * @property status The employment's current status.
 * @property probationEndDate Optional employment's probation end date.
 * @property workModality The employment's work modality.
 */
@Serializable
data class EmploymentRequest(
    val period: Period,
    val status: EmploymentStatus,
    val probationEndDate: KLocalDate? = null,
    val workModality: WorkModality
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
