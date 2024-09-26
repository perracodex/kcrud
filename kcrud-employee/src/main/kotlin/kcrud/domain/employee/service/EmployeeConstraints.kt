/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.domain.employee.service

import kcrud.core.errors.AppException
import kcrud.core.errors.CompositeAppException
import kcrud.core.errors.validators.EmailValidator
import kcrud.core.errors.validators.PhoneValidator
import kcrud.domain.contact.model.ContactRequest
import kcrud.domain.employee.errors.EmployeeError
import kcrud.domain.employee.model.EmployeeRequest
import kcrud.domain.employee.repository.EmployeeRepository
import kcrud.domain.employee.repository.IEmployeeRepository
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
     * @param repository The [EmployeeRepository] to perform additional checks.
     * @return A [Result] with verification state.
     */
    fun check(
        employeeId: Uuid?,
        request: EmployeeRequest,
        reason: String,
        repository: IEmployeeRepository,
    ): Result<Unit> {
        val errors: MutableList<AppException> = mutableListOf()

        // Check the work email.
        checkWorkEmail(
            employeeId = employeeId,
            workEmail = request.workEmail,
            reason = reason,
            repository = repository,
            errors = errors
        )

        // Check the contact's personal email and phone.
        request.contact?.let { contact ->
            checkContact(
                employeeId = employeeId,
                contactRequest = contact,
                reason = reason,
                errors = errors
            )
        }

        if (errors.isNotEmpty()) {
            return Result.failure(CompositeAppException(errors))
        }

        return Result.success(Unit)
    }

    /**
     * Verifies the work email of the employee. Format and uniqueness are checked.
     *
     * @param employeeId The ID of the employee being verified. `null` if the employee is new.
     * @param workEmail The work email to be verified.
     * @param reason The reason for the verification. To be included in error messages.
     * @param repository The [EmployeeRepository] to check for uniqueness.
     * @param errors The list of errors to append to.
     */
    private fun checkWorkEmail(
        employeeId: Uuid?,
        workEmail: String,
        reason: String,
        repository: IEmployeeRepository,
        errors: MutableList<AppException>
    ) {
        // Check the work email format.
        EmailValidator.check(value = workEmail).onFailure { error ->
            errors.add(
                EmployeeError.InvalidEmail(
                    employeeId = employeeId,
                    email = workEmail,
                    field = "workEmail",
                    reason = reason,
                    cause = error
                )
            )
        }

        // Check if the work email is already in use.
        repository.findByWorkEmail(workEmail, excludeEmployeeId = employeeId)?.let { employee ->
            errors.add(
                EmployeeError.DuplicateWorkEmail(
                    affectedEmployeeId = employeeId,
                    usedByEmployeeId = employee.id,
                    workEmail = workEmail,
                    field = "workEmail",
                    reason = reason,
                    cause = null
                )
            )
        }
    }

    /**
     * Verifies the contact details of the employee.
     *
     * @param employeeId The ID of the employee being verified. `null` if the employee is new.
     * @param contactRequest The [ContactRequest] details to be verified.
     * @param reason The reason for the verification. To be included in error messages.
     * @param errors The list of errors to append to.
     */
    private fun checkContact(
        employeeId: Uuid?,
        contactRequest: ContactRequest,
        reason: String,
        errors: MutableList<AppException>
    ) {
        // Check the contact's personal email and phone.
        EmailValidator.check(value = contactRequest.email).onFailure { error ->
            errors.add(
                EmployeeError.InvalidEmail(
                    employeeId = employeeId,
                    email = contactRequest.email,
                    field = "contact.email",
                    reason = reason,
                    cause = error
                )
            )
        }

        PhoneValidator.check(value = contactRequest.phone).onFailure { error ->
            errors.add(
                EmployeeError.InvalidPhoneNumber(
                    employeeId = employeeId,
                    phone = contactRequest.phone,
                    field = "contact.phone",
                    reason = reason,
                    cause = error
                )
            )
        }
    }
}
