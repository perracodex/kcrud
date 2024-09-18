/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.domain.employee.service

import kcrud.base.env.CallContext
import kcrud.base.env.Tracer
import kcrud.base.persistence.pagination.Page
import kcrud.base.persistence.pagination.Pageable
import kcrud.base.persistence.validators.IValidator
import kcrud.base.persistence.validators.impl.EmailValidator
import kcrud.base.persistence.validators.impl.PhoneValidator
import kcrud.domain.employee.errors.EmployeeError
import kcrud.domain.employee.model.Employee
import kcrud.domain.employee.model.EmployeeFilterSet
import kcrud.domain.employee.model.EmployeeRequest
import kcrud.domain.employee.repository.IEmployeeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.uuid.Uuid

/**
 * Employee service, where all the employee business logic should be defined.
 */
public class EmployeeService internal constructor(
    @Suppress("unused") private val context: CallContext,
    private val employeeRepository: IEmployeeRepository
) {
    private val tracer = Tracer<EmployeeService>()

    /**
     * Retrieves an employee by its ID.
     *
     * @param employeeId The ID of the employee to be retrieved.
     * @return The resolved [Employee] if found, null otherwise.
     */
    public suspend fun findById(employeeId: Uuid): Employee? = withContext(Dispatchers.IO) {
        return@withContext employeeRepository.findById(employeeId = employeeId)
    }

    /**
     * Retrieves all employees.
     *
     * @param pageable The pagination options to be applied, or null for a single all-in-one page.
     * @return List of [Employee] entries.
     */
    public suspend fun findAll(pageable: Pageable? = null): Page<Employee> = withContext(Dispatchers.IO) {
        return@withContext employeeRepository.findAll(pageable = pageable)
    }

    /**
     * Retrieves all employees matching the provided [filterSet].
     *
     * @param filterSet The [EmployeeFilterSet] to be applied.
     * @param pageable The pagination options to be applied, or null for a single all-in-one page.
     * @return List of [Employee] entries.
     */
    public suspend fun search(filterSet: EmployeeFilterSet, pageable: Pageable? = null): Page<Employee> {
        return withContext(Dispatchers.IO) {
            employeeRepository.search(filterSet = filterSet, pageable = pageable)
        }
    }

    /**
     * Creates a new employee.
     *
     * @param request The [EmployeeRequest] to be created.
     * @return A [Result] with the created [Employee], or an error on failure.
     */
    public suspend fun create(request: EmployeeRequest): Result<Employee> {
        tracer.debug("Creating a new employee.")

        return verifyIntegrity(
            employeeId = null,
            request = request,
            reason = "Create Employee."
        ).fold(
            onSuccess = {
                runCatching {
                    withContext(Dispatchers.IO) {
                        employeeRepository.create(request = request)
                    }
                }
            },
            onFailure = { error ->
                tracer.error(message = "Failed to create a new employee.", cause = error)
                Result.failure(error)
            }
        )
    }

    /**
     * Updates an employee's details.
     *
     * @param employeeId The ID of the employee to be updated.
     * @param request The new [EmployeeRequest] to be updated.
     * @return A [Result] with the updated [Employee] or null if such does not exist; or an error on failure.
     */
    public suspend fun update(
        employeeId: Uuid,
        request: EmployeeRequest
    ): Result<Employee?> {
        tracer.debug("Updating employee with ID: $employeeId.")

        return verifyIntegrity(
            employeeId = employeeId,
            request = request,
            reason = "Update Employee."
        ).fold(
            onSuccess = {
                runCatching {
                    withContext(Dispatchers.IO) {
                        employeeRepository.update(employeeId = employeeId, request = request)
                    }
                }
            },
            onFailure = { error ->
                tracer.error(message = "Failed to update employee.", cause = error)
                Result.failure(error)
            }
        )
    }

    /**
     * Deletes an employee using the provided ID.
     *
     * @param employeeId The ID of the employee to be deleted.
     * @return The number of delete records.
     */
    public suspend fun delete(employeeId: Uuid): Int = withContext(Dispatchers.IO) {
        tracer.debug("Deleting employee with ID: $employeeId.")
        return@withContext employeeRepository.delete(employeeId = employeeId)
    }

    /**
     * Deletes all employees.
     *
     * @return The number of deleted records.
     */
    public suspend fun deleteAll(): Int = withContext(Dispatchers.IO) {
        tracer.debug("Deleting all employees.")
        return@withContext employeeRepository.deleteAll()
    }

    /**
     * Retrieves the total count of employees.
     *
     * @return The total count of existing records.
     */
    public suspend fun count(): Int = withContext(Dispatchers.IO) {
        return@withContext employeeRepository.count()
    }

    /**
     * Verifies if the employee's fields.
     *
     * @param employeeId The ID of the employee being verified.
     * @param request The [EmployeeRequest] details to be verified.
     * @param reason The reason for the email verification. To be included in the error message.
     * @return A [Result] indicating if the employee's fields are valid.
     * @return A [Result] with verification state.
     */
    private fun verifyIntegrity(employeeId: Uuid?, request: EmployeeRequest, reason: String): Result<Unit> {
        request.contact?.let { contact ->
            val phone: String = contact.phone
            val phoneValidation: IValidator.Result = PhoneValidator.validate(value = phone)
            if (phoneValidation is IValidator.Result.Failure) {
                return Result.failure(
                    EmployeeError.InvalidPhoneFormat(
                        employeeId = employeeId,
                        phone = phone,
                        reason = "$reason ${phoneValidation.reason}"
                    )
                )
            }

            // Note: The email could also be verified via the EmailString serializer before
            // reaching this point, which would raise a generic error for the email field.
            // However, this verification shows how to raise a custom error for the email field
            // with a concrete error code and description.
            // The difference between this approach or using the EmailString serializer,
            // is that the serializer is a generic one, and it is not aware of the context in which
            // it is being used, so it cannot provide a more concrete error code and description.
            val email: String = contact.email
            val emailValidation: IValidator.Result = EmailValidator.validate(value = email)
            if (emailValidation is IValidator.Result.Failure) {
                return Result.failure(
                    EmployeeError.InvalidEmailFormat(
                        employeeId = employeeId,
                        email = email,
                        reason = "$reason ${emailValidation.reason}"
                    )
                )
            }
        }

        return Result.success(Unit)
    }
}
