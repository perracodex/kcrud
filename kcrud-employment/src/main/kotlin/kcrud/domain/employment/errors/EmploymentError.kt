/*
 * Copyright (c) 2023-Present Perraco. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.domain.employment.errors

import io.ktor.http.*
import kcrud.base.errors.BaseError
import kcrud.base.errors.ErrorCodeRegistry
import kcrud.base.utils.KLocalDate
import java.util.*

/**
 * Concrete errors for the Employment domain.
 *
 * @property status The [HttpStatusCode] associated with this error.
 * @property code A unique code identifying the type of error.
 * @property description A human-readable description of the error.
 */
sealed class EmploymentError(
    status: HttpStatusCode,
    code: String,
    description: String
) : BaseError(status = status, code = code, description = description) {

    /**
     * Error for when an employment is not found for a concrete employee.
     *
     * @property employeeId The affected employee id.
     * @property employmentId The employment id that was not found.
     */
    data class EmploymentNotFound(val employeeId: UUID, val employmentId: UUID) : EmploymentError(
        status = HttpStatusCode.NotFound,
        code = "${TAG}ENF",
        description = "Employment not found. Employee Id: $employeeId. Employment Id: $employmentId."
    )

    /**
     * Error when there is an inconsistency in the period dates,
     * where the end date is prior to the start date.
     *
     * @property employeeId The affected employee id.
     * @property employmentId The employment id associated with the error.
     * @property startDate The start date of the employment period.
     * @property endDate The end date of the employment period.
     */
    data class PeriodDatesMismatch(
        val employeeId: UUID,
        val employmentId: UUID?,
        val startDate: KLocalDate,
        val endDate: KLocalDate
    ) : EmploymentError(
        status = HttpStatusCode.BadRequest,
        code = "${TAG}PDM",
        description = "Employment end date cannot be prior to the start date. " +
                "Employee Id: $employeeId. Employment Id: $employmentId. " +
                "Start Date: $startDate. End Date: $endDate."
    )

    /**
     * Error when the probation end date is prior to the employment start date.
     *
     * @property employeeId The affected employee id.
     * @property employmentId The employment id associated with the error.
     * @property startDate The start date of the employment period.
     * @property probationEndDate The probation end date of the employment period.
     */
    data class InvalidProbationEndDate(
        val employeeId: UUID,
        val employmentId: UUID?,
        val startDate: KLocalDate,
        val probationEndDate: KLocalDate,
    ) : EmploymentError(
        status = HttpStatusCode.BadRequest,
        code = "${TAG}IPD",
        description = "Employment probation end date cannot be prior to the start date. " +
                "Employee Id: $employeeId. Employment Id: $employmentId. " +
                "Start Date: $startDate. Probation End Date: $probationEndDate."
    )

    companion object {
        private const val TAG: String = "EMT."

        init {
            ErrorCodeRegistry.registerTag(tag = TAG)
        }
    }
}
