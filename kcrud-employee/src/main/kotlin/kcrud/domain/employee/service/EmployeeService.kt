/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.domain.employee.service

import kcrud.access.system.SessionContext
import kcrud.base.infrastructure.errors.BaseError
import kcrud.base.infrastructure.utils.Tracer
import kcrud.base.persistence.pagination.Page
import kcrud.base.persistence.pagination.Pageable
import kcrud.base.persistence.validators.IValidator
import kcrud.base.persistence.validators.implementations.EmailValidator
import kcrud.base.persistence.validators.implementations.PhoneValidator
import kcrud.domain.employee.entities.EmployeeEntity
import kcrud.domain.employee.entities.EmployeeFilterSet
import kcrud.domain.employee.entities.EmployeeRequest
import kcrud.domain.employee.errors.EmployeeError
import kcrud.domain.employee.repository.IEmployeeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

/**
 * Employee service, where all the employee business logic should be defined.
 */
class EmployeeService(
    @Suppress("unused") private val sessionContext: SessionContext,
    private val employeeRepository: IEmployeeRepository
) {
    private val tracer = Tracer<EmployeeService>()

    /**
     * Retrieves an employee entity by its ID.
     *
     * @param employeeId The ID of the employee to be retrieved.
     * @return The resolved [EmployeeEntity] if found, null otherwise.
     */
    suspend fun findById(employeeId: UUID): EmployeeEntity? = withContext(Dispatchers.IO) {
        employeeRepository.findById(employeeId = employeeId)
    }

    /**
     * Retrieves all employee entities.
     *
     * @param pageable The pagination options to be applied.
     *                 If not provided, a single page with the result will be returned.
     * @return List of [EmployeeEntity] entries.
     */
    suspend fun findAll(pageable: Pageable? = null): Page<EmployeeEntity> = withContext(Dispatchers.IO) {
        employeeRepository.findAll(pageable = pageable)
    }

    /**
     * Retrieves all employee entities matching the provided [filterSet].
     *
     * @param filterSet The [EmployeeFilterSet] to be applied.
     * @return List of [EmployeeEntity] entries.
     */
    suspend fun filter(filterSet: EmployeeFilterSet): Page<EmployeeEntity> = withContext(Dispatchers.IO) {
        employeeRepository.filter(filterSet = filterSet)
    }

    /**
     * Creates a new employee.
     *
     * @param employeeRequest The employee to be created.
     * @return The ID of the created employee.
     */
    suspend fun create(employeeRequest: EmployeeRequest): EmployeeEntity = withContext(Dispatchers.IO) {
        tracer.debug("Creating a new employee.")
        verifyIntegrity(employeeId = null, employeeRequest = employeeRequest, reason = "Create Employee.")
        val employeeId: UUID = employeeRepository.create(employeeRequest = employeeRequest)
        findById(employeeId = employeeId)!!
    }

    /**
     * Updates an employee's details.
     *
     * @param employeeId The ID of the employee to be updated.
     * @param employeeRequest The new details for the employee.
     * @return The number of updated records.
     */
    suspend fun update(
        employeeId: UUID,
        employeeRequest: EmployeeRequest
    ): EmployeeEntity? = withContext(Dispatchers.IO) {
        tracer.debug("Updating employee with ID: $employeeId.")
        verifyIntegrity(employeeId = employeeId, employeeRequest = employeeRequest, reason = "Update Employee.")
        val updatedCount: Int = employeeRepository.update(employeeId = employeeId, employeeRequest = employeeRequest)
        if (updatedCount > 0) findById(employeeId = employeeId) else null
    }

    /**
     * Deletes an employee using the provided ID.
     *
     * @param employeeId The ID of the employee to be deleted.
     * @return The number of delete records.
     */
    suspend fun delete(employeeId: UUID): Int = withContext(Dispatchers.IO) {
        tracer.debug("Deleting employee with ID: $employeeId.")
        employeeRepository.delete(employeeId = employeeId)
    }

    /**
     * Deletes all employees.
     *
     * @return The number of deleted records.
     */
    suspend fun deleteAll(): Int = withContext(Dispatchers.IO) {
        tracer.debug("Deleting all employees.")
        employeeRepository.deleteAll()
    }

    /**
     * Retrieves the total count of employees.
     *
     * @return The total count of existing records.
     */
    suspend fun count(): Int = withContext(Dispatchers.IO) {
        employeeRepository.count()
    }

    /**
     * Verifies if the employee's fields.
     *
     * @param employeeId The ID of the employee being verified.
     * @param employeeRequest The employee request details.
     * @param reason The reason for the email verification.
     * @throws BaseError If any of the fields is invalid.
     */
    private fun verifyIntegrity(employeeId: UUID?, employeeRequest: EmployeeRequest, reason: String) {
        employeeRequest.contact?.let { contact ->
            val phone: String = contact.phone
            val phoneValidation: IValidator.Result = PhoneValidator.validate(value = phone)
            if (phoneValidation is IValidator.Result.Failure) {
                EmployeeError.InvalidPhoneFormat(employeeId = employeeId, phone = phone)
                    .raise(reason = "$reason ${phoneValidation.reason}")
            }

            // Note: For sake of the example, we are already validating the email via the EmailString serializer
            // defined in the ContactRequest entity.
            // So, this validation is not really necessary and could be removed.
            // However, this verification shows how to raise a custom error for the email field
            // with a concrete error code and description.
            // The difference between this approach and the one used in the EmailString serializer,
            // is that the serializer is a generic one, and it is not aware of the context in which
            // it is being used, so it cannot provide a more concrete error code and description
            // as if using the following approach.
            // Also, there is another example of email validation at the Table column level.
            val email: String = contact.email
            val emailValidation: IValidator.Result = EmailValidator.validate(value = email)
            if (emailValidation is IValidator.Result.Failure) {
                EmployeeError.InvalidEmailFormat(employeeId = employeeId, email = email)
                    .raise(reason = "$reason ${emailValidation.reason}")
            }
        }
    }
}
