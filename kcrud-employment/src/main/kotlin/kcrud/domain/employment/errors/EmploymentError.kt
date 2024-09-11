/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.domain.employment.errors

import io.ktor.http.*
import kcrud.base.errors.AppException
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
) : AppException(
    status = status,
    context = "EMPLOYMENT",
    code = code,
    description = description,
    reason = reason,
    cause = cause
) {
    /**
     * Error for when an employment is not found for a concrete employee.
     *
     * @param employeeId The affected employee id.
     * @param employmentId The employment id that was not found.
     */
    class EmploymentNotFound(
        employeeId: Uuid,
        employmentId: Uuid,
        reason: String? = null,
        cause: Throwable? = null
    ) : EmploymentError(
        status = HttpStatusCode.NotFound,
        code = "EMPLOYMENT_NOT_FOUND",
        description = "Employment not found. Employee Id: $employeeId. Employment Id: $employmentId.",
        reason = reason,
        cause = cause
    )

    /**
     * Error when there is an inconsistency in the period dates,
     * where the end date is prior to the start date.
     *
     * @param employeeId The affected employee id.
     * @param employmentId The employment id associated with the error.
     * @param startDate The start date of the employment period.
     * @param endDate The end date of the employment period.
     */
    class PeriodDatesMismatch(
        employeeId: Uuid,
        employmentId: Uuid?,
        startDate: KLocalDate,
        endDate: KLocalDate,
        reason: String? = null,
        cause: Throwable? = null
    ) : EmploymentError(
        status = HttpStatusCode.BadRequest,
        code = "PERIOD_DATES_MISMATCH",
        description = "Employment end date cannot be prior to the start date. " +
                "Employee Id: $employeeId. Employment Id: $employmentId. " +
                "Start Date: $startDate. End Date: $endDate.",
        reason = reason,
        cause = cause
    )

    /**
     * Error when the probation end date is prior to the employment start date.
     *
     * @param employeeId The affected employee id.
     * @param employmentId The employment id associated with the error.
     * @param startDate The start date of the employment period.
     * @param probationEndDate The probation end date of the employment period.
     */
    class InvalidProbationEndDate(
        employeeId: Uuid,
        employmentId: Uuid?,
        startDate: KLocalDate,
        probationEndDate: KLocalDate,
        reason: String? = null,
        cause: Throwable? = null
    ) : EmploymentError(
        status = HttpStatusCode.BadRequest,
        code = "INVALID_PROBATION_END_DATE",
        description = "Employment probation end date cannot be prior to the start date. " +
                "Employee Id: $employeeId. Employment Id: $employmentId. " +
                "Start Date: $startDate. Probation End Date: $probationEndDate.",
        reason = reason,
        cause = cause
    )
}
