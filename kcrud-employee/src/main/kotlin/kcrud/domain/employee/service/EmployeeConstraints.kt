/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.domain.employee.service

import kcrud.base.errors.AppException
import kcrud.base.errors.CompositeAppException
import kcrud.base.persistence.validators.EmailValidator
import kcrud.base.persistence.validators.PhoneValidator
import kcrud.domain.employee.errors.EmployeeError
import kcrud.domain.employee.model.EmployeeRequest
import kotlin.uuid.Uuid

/**
 * Provides verification methods for employee-related operations.
 */
internal object EmployeeConstraints {
    /**
     * Verifies if the integrity of the given [request] is valid.
     *
     * Note: For the email verification, it could be done alternatively with the EmailString,
     * but such would show only a generic error to the client, without any contextual information.
     *
     * @param employeeId The ID of the employee being verified.
     * @param request The [EmployeeRequest] details to be verified.
     * @param reason The reason for the verification. To be included in error messages.
     * @return A [Result] with verification state.
     */
    fun check(employeeId: Uuid?, request: EmployeeRequest, reason: String): Result<Unit> {
        request.contact?.let { contact ->
            val errors: MutableList<AppException> = mutableListOf()

            PhoneValidator.check(value = contact.phone).onFailure { error ->
                errors.add(
                    EmployeeError.InvalidPhoneFormat(
                        employeeId = employeeId,
                        phone = contact.phone,
                        reason = reason,
                        cause = error
                    )
                )
            }

            EmailValidator.check(value = contact.email).onFailure { error ->
                errors.add(
                    EmployeeError.InvalidEmailFormat(
                        employeeId = employeeId,
                        email = contact.email,
                        reason = reason,
                        cause = error
                    )
                )
            }

            if (errors.isNotEmpty()) {
                return Result.failure(CompositeAppException(errors))
            }
        }

        return Result.success(Unit)
    }
}