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
 * @param statusCode The [HttpStatusCode] associated with this error.
 * @param errorCode A unique code identifying the type of error.
 * @param description A human-readable description of the error.
 * @param reason An optional human-readable reason for the exception, providing more context.
 * @param cause The underlying cause of the exception, if any.
 */
internal sealed class EmploymentError(
    statusCode: HttpStatusCode,
    errorCode: String,
    description: String,
    reason: String? = null,
    cause: Throwable? = null
) : AppException(
    statusCode = statusCode,
    errorCode = errorCode,
    context = "EMPLOYMENT",
    description = description,
    reason = reason,
    error = cause
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
        statusCode = STATUS_CODE,
        errorCode = ERROR_CODE,
        description = "Employment not found. Employee Id: $employeeId. Employment Id: $employmentId.",
        reason = reason,
        cause = cause
    ) {
        companion object {
            val STATUS_CODE: HttpStatusCode = HttpStatusCode.NotFound
            const val ERROR_CODE: String = "EMPLOYMENT_NOT_FOUND"
        }
    }

    /**
     * Error for when an employee is not found for a concrete employment.
     *
     * @param employeeId The employee id that was not found.
     */
    class EmployeeNotFound(
        employeeId: Uuid,
        reason: String? = null,
        cause: Throwable? = null
    ) : EmploymentError(
        statusCode = STATUS_CODE,
        errorCode = ERROR_CODE,
        description = "Employee not found. Employee Id: $employeeId.",
        reason = reason,
        cause = cause
    ) {
        companion object {
            val STATUS_CODE: HttpStatusCode = HttpStatusCode.NotFound
            const val ERROR_CODE: String = "EMPLOYEE_NOT_FOUND"
        }
    }

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
        statusCode = STATUS_CODE,
        errorCode = ERROR_CODE,
        description = "Employment end date cannot be prior to the start date. " +
                "Employee Id: $employeeId. Employment Id: $employmentId. " +
                "Start Date: $startDate. End Date: $endDate.",
        reason = reason,
        cause = cause
    ) {
        companion object {
            val STATUS_CODE: HttpStatusCode = HttpStatusCode.BadRequest
            const val ERROR_CODE: String = "PERIOD_DATES_MISMATCH"
        }
    }

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
        statusCode = STATUS_CODE,
        errorCode = ERROR_CODE,
        description = "Employment probation end date cannot be prior to the start date. " +
                "Employee Id: $employeeId. Employment Id: $employmentId. " +
                "Start Date: $startDate. Probation End Date: $probationEndDate.",
        reason = reason,
        cause = cause
    ) {
        companion object {
            val STATUS_CODE: HttpStatusCode = HttpStatusCode.BadRequest
            const val ERROR_CODE: String = "INVALID_PROBATION_END_DATE"
        }
    }
}
