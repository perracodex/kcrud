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
 * @param field Optional field identifier, typically the input field that caused the error.
 * @param reason Optional human-readable reason for the exception, providing more context.
 * @param cause Optional underlying cause of the exception, if any.
 */
internal sealed class EmployeeError(
    statusCode: HttpStatusCode,
    errorCode: String,
    description: String,
    field: String? = null,
    reason: String? = null,
    cause: Throwable? = null
) : AppException(
    statusCode = statusCode,
    errorCode = errorCode,
    context = "EMPLOYEE",
    description = description,
    field = field,
    reason = reason,
    cause = cause
) {
    /**
     * Error for when an employee is not found.
     *
     * @param employeeId The employee id that was not found.
     * @param reason Optional human-readable reason for the exception, providing more context.
     * @param cause Optional underlying cause of the exception, if any.
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
     * Error for when an email invalid.
     *
     * @param employeeId The affected employee id.
     * @param email The invalid email.
     * @param field Optional field identifier, typically the input field that caused the error.
     * @param reason Optional human-readable reason for the exception, providing more context.
     * @param cause Optional underlying cause of the exception, if any.
     */
    class InvalidEmail(
        employeeId: Uuid?,
        email: String,
        field: String? = null,
        reason: String? = null,
        cause: Throwable? = null
    ) : EmployeeError(
        statusCode = STATUS_CODE,
        errorCode = ERROR_CODE,
        description = "Invalid email: '$email'. Employee Id: $employeeId",
        field = field,
        reason = reason,
        cause = cause
    ) {
        companion object {
            val STATUS_CODE: HttpStatusCode = HttpStatusCode.BadRequest
            const val ERROR_CODE: String = "INVALID_EMAIL"
        }
    }

    /**
     * Error for when a phone number is invalid, typically due to an incorrect format.
     *
     * @param employeeId The affected employee id.
     * @param phone The invalid phone number.
     * @param field Optional field identifier, typically the input field that caused the error.
     * @param reason Optional human-readable reason for the exception, providing more context.
     * @param cause Optional underlying cause of the exception, if any.
     */
    class InvalidPhoneNumber(
        employeeId: Uuid?,
        phone: String,
        field: String? = null,
        reason: String? = null,
        cause: Throwable? = null
    ) : EmployeeError(
        statusCode = STATUS_CODE,
        errorCode = ERROR_CODE,
        description = "Invalid phone number: '$phone'. Employee Id: $employeeId",
        field = field,
        reason = reason,
        cause = cause
    ) {
        companion object {
            val STATUS_CODE: HttpStatusCode = HttpStatusCode.BadRequest
            const val ERROR_CODE: String = "INVALID_PHONE_NUMBER"
        }
    }
}
