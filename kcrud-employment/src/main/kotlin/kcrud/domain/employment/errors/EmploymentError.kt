/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.domain.employment.errors

import io.ktor.http.*
import kcrud.base.errors.AppException
import kcrud.base.errors.ErrorCodeRegistry
import kcrud.base.utils.KLocalDate
import kotlin.uuid.Uuid

/**
 * Concrete errors for the Employment domain.
 *
 * @property status The [HttpStatusCode] associated with this error.
 * @property code A unique code identifying the type of error.
 * @property description A human-readable description of the error.
 * @property reason An optional human-readable reason for the exception, providing more context.
 * @property cause The underlying cause of the exception, if any.
 */
internal sealed class EmploymentError(
    status: HttpStatusCode,
    code: String,
    description: String,
    reason: String? = null,
    cause: Throwable? = null
) : AppException(status = status, code = code, description = description, reason = reason, cause = cause) {

    /**
     * Error for when an employment is not found for a concrete employee.
     *
     * @property employeeId The affected employee id.
     * @property employmentId The employment id that was not found.
     */
    class EmploymentNotFound(
        val employeeId: Uuid,
        val employmentId: Uuid,
        reason: String? = null,
        cause: Throwable? = null
    ) : EmploymentError(
        status = HttpStatusCode.NotFound,
        code = "${TAG}ENF",
        description = "Employment not found. Employee Id: $employeeId. Employment Id: $employmentId.",
        reason = reason,
        cause = cause
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
    class PeriodDatesMismatch(
        val employeeId: Uuid,
        val employmentId: Uuid?,
        val startDate: KLocalDate,
        val endDate: KLocalDate,
        reason: String? = null,
        cause: Throwable? = null
    ) : EmploymentError(
        status = HttpStatusCode.BadRequest,
        code = "${TAG}PDM",
        description = "Employment end date cannot be prior to the start date. " +
                "Employee Id: $employeeId. Employment Id: $employmentId. " +
                "Start Date: $startDate. End Date: $endDate.",
        reason = reason,
        cause = cause
    )

    /**
     * Error when the probation end date is prior to the employment start date.
     *
     * @property employeeId The affected employee id.
     * @property employmentId The employment id associated with the error.
     * @property startDate The start date of the employment period.
     * @property probationEndDate The probation end date of the employment period.
     */
    class InvalidProbationEndDate(
        val employeeId: Uuid,
        val employmentId: Uuid?,
        val startDate: KLocalDate,
        val probationEndDate: KLocalDate,
        reason: String? = null,
        cause: Throwable? = null
    ) : EmploymentError(
        status = HttpStatusCode.BadRequest,
        code = "${TAG}IPD",
        description = "Employment probation end date cannot be prior to the start date. " +
                "Employee Id: $employeeId. Employment Id: $employmentId. " +
                "Start Date: $startDate. Probation End Date: $probationEndDate.",
        reason = reason,
        cause = cause
    )

    companion object {
        private const val TAG: String = "EMT."

        init {
            ErrorCodeRegistry.registerTag(tag = TAG)
        }
    }
}
