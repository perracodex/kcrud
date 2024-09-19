/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.domain.employee.errors

import io.ktor.http.*
import kcrud.base.errors.AppException
import kotlin.uuid.Uuid

/**
 * Concrete errors for the Employee domain.
 *
 * @param statusCode The [HttpStatusCode] associated with this error.
 * @param errorCode A unique code identifying the type of error.
 * @param description A human-readable description of the error.
 * @param reason An optional human-readable reason for the exception, providing more context.
 * @param cause The underlying cause of the exception, if any.
 */
internal sealed class EmployeeError(
    statusCode: HttpStatusCode,
    errorCode: String,
    description: String,
    reason: String? = null,
    cause: Throwable? = null
) : AppException(
    statusCode = statusCode,
    errorCode = errorCode,
    context = "EMPLOYEE",
    description = description,
    reason = reason,
    error = cause
) {
    /**
     * Error for when an employee is not found.
     *
     * @param employeeId The employee id that was not found.
     */
    class EmployeeNotFound(
        employeeId: Uuid,
        reason: String? = null,
        cause: Throwable? = null
    ) : EmployeeError(
        statusCode = STATUS_CODE,
        errorCode = ERROR_CODE,
        description = "Employee not found. Employee Id: $employeeId",
        reason = reason,
        cause = cause
    ) {
        companion object {
            val STATUS_CODE: HttpStatusCode = HttpStatusCode.NotFound
            const val ERROR_CODE: String = "EMPLOYEE_NOT_FOUND"
        }
    }

    /**
     * Error for when an email has an invalid format.
     *
     * @param employeeId The affected employee id.
     * @param email The email that is already registered.
     */
    class InvalidEmailFormat(
        employeeId: Uuid?,
        email: String,
        reason: String? = null,
        cause: Throwable? = null
    ) : EmployeeError(
        statusCode = STATUS_CODE,
        errorCode = ERROR_CODE,
        description = "Invalid email format: '$email'. Employee Id: $employeeId",
        reason = reason,
        cause = cause
    ) {
        companion object {
            val STATUS_CODE: HttpStatusCode = HttpStatusCode.BadRequest
            const val ERROR_CODE: String = "INVALID_EMAIL_FORMAT"
        }
    }

    /**
     * Error for when a phone has an invalid format.
     *
     * @param employeeId The affected employee id.
     * @param phone The phone value with the invalid format.
     */
    class InvalidPhoneFormat(
        employeeId: Uuid?,
        phone: String,
        reason: String? = null,
        cause: Throwable? = null
    ) : EmployeeError(
        statusCode = STATUS_CODE,
        errorCode = ERROR_CODE,
        description = "Invalid phone number: '$phone'. Employee Id: $employeeId",
        reason = reason,
        cause = cause
    ) {
        companion object {
            val STATUS_CODE: HttpStatusCode = HttpStatusCode.BadRequest
            const val ERROR_CODE: String = "INVALID_PHONE_FORMAT"
        }
    }
}
