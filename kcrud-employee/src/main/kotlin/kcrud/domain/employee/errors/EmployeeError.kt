/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.domain.employee.errors

import io.ktor.http.*
import kcrud.base.errors.AppException
import kcrud.base.errors.ErrorCodeRegistry
import java.util.*

/**
 * Concrete errors for the Employee domain.
 *
 * @property status The [HttpStatusCode] associated with this error.
 * @property code A unique code identifying the type of error.
 * @property description A human-readable description of the error.
 * @property reason An optional human-readable reason for the exception, providing more context.
 * @property cause The underlying cause of the exception, if any.
 */
sealed class EmployeeError(
    status: HttpStatusCode,
    code: String,
    description: String,
    reason: String? = null,
    cause: Throwable? = null
) : AppException(status = status, code = code, description = description, reason = reason, cause = cause) {

    /**
     * Error for when an employee is not found.
     *
     * @property employeeId The employee id that was not found.
     */
    class EmployeeNotFound(
        val employeeId: UUID,
        reason: String? = null,
        cause: Throwable? = null
    ) : EmployeeError(
        status = HttpStatusCode.NotFound,
        code = "${TAG}ENF",
        description = "Employee not found. Employee Id: $employeeId",
        reason = reason,
        cause = cause
    )

    /**
     * Error for when an email has an invalid format.
     *
     * @property employeeId The affected employee id.
     * @property email The email that is already registered.
     */
    class InvalidEmailFormat(
        val employeeId: UUID?,
        val email: String,
        reason: String? = null,
        cause: Throwable? = null
    ) : EmployeeError(
        status = HttpStatusCode.BadRequest,
        code = "${TAG}IEF",
        description = "Invalid email format: '$email'. Employee Id: $employeeId",
        reason = reason,
        cause = cause
    )

    /**
     * Error for when a phone has an invalid format.
     *
     * @property employeeId The affected employee id.
     * @property phone The phone value with the invalid format.
     */
    class InvalidPhoneFormat(
        val employeeId: UUID?,
        val phone: String,
        reason: String? = null,
        cause: Throwable? = null
    ) : EmployeeError(
        status = HttpStatusCode.BadRequest,
        code = "${TAG}IPF",
        description = "Invalid phone format: '$phone'. Employee Id: $employeeId",
        reason = reason,
        cause = cause
    )

    companion object {
        private const val TAG: String = "EMP."

        init {
            ErrorCodeRegistry.registerTag(tag = TAG)
        }
    }
}
