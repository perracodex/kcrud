/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.domain.employee.service

import kcrud.base.env.SessionContext
import kcrud.base.env.Tracer
import kcrud.base.persistence.pagination.Page
import kcrud.base.persistence.pagination.Pageable
import kcrud.base.persistence.validators.IValidator
import kcrud.base.persistence.validators.impl.EmailValidator
import kcrud.base.persistence.validators.impl.PhoneValidator
import kcrud.domain.employee.entity.EmployeeEntity
import kcrud.domain.employee.entity.EmployeeFilterSet
import kcrud.domain.employee.entity.EmployeeRequest
import kcrud.domain.employee.errors.EmployeeError
import kcrud.domain.employee.repository.IEmployeeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.uuid.Uuid

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
    suspend fun findById(employeeId: Uuid): EmployeeEntity? = withContext(Dispatchers.IO) {
        return@withContext employeeRepository.findById(employeeId = employeeId)
    }

    /**
     * Retrieves all employee entities.
     *
     * @param pageable The pagination options to be applied, or null for a single all-in-one page.
     * @return List of [EmployeeEntity] entries.
     */
    suspend fun findAll(pageable: Pageable? = null): Page<EmployeeEntity> = withContext(Dispatchers.IO) {
        return@withContext employeeRepository.findAll(pageable = pageable)
    }

    /**
     * Retrieves all employee entities matching the provided [filterSet].
     *
     * @param filterSet The [EmployeeFilterSet] to be applied.
     * @param pageable The pagination options to be applied, or null for a single all-in-one page.
     * @return List of [EmployeeEntity] entries.
     */
    suspend fun search(filterSet: EmployeeFilterSet, pageable: Pageable? = null): Page<EmployeeEntity> = withContext(Dispatchers.IO) {
        return@withContext employeeRepository.search(filterSet = filterSet, pageable = pageable)
    }

    /**
     * Creates a new employee.
     *
     * @param employeeRequest The employee to be created.
     * @return The ID of the created employee.
     */
    suspend fun create(employeeRequest: EmployeeRequest): EmployeeEntity {
        tracer.debug("Creating a new employee.")

        verifyIntegrity(employeeId = null, employeeRequest = employeeRequest, reason = "Create Employee.")

        return withContext(Dispatchers.IO) {
            val employeeId: Uuid = employeeRepository.create(employeeRequest = employeeRequest)
            return@withContext findById(employeeId = employeeId)!!
        }
    }

    /**
     * Updates an employee's details.
     *
     * @param employeeId The ID of the employee to be updated.
     * @param employeeRequest The new details for the employee.
     * @return The number of updated records.
     */
    suspend fun update(
        employeeId: Uuid,
        employeeRequest: EmployeeRequest
    ): EmployeeEntity? {
        tracer.debug("Updating employee with ID: $employeeId.")

        verifyIntegrity(employeeId = employeeId, employeeRequest = employeeRequest, reason = "Update Employee.")

        return withContext(Dispatchers.IO) {
            val updatedCount: Int = employeeRepository.update(employeeId = employeeId, employeeRequest = employeeRequest)
            return@withContext if (updatedCount > 0) findById(employeeId = employeeId) else null
        }
    }

    /**
     * Deletes an employee using the provided ID.
     *
     * @param employeeId The ID of the employee to be deleted.
     * @return The number of delete records.
     */
    suspend fun delete(employeeId: Uuid): Int = withContext(Dispatchers.IO) {
        tracer.debug("Deleting employee with ID: $employeeId.")
        return@withContext employeeRepository.delete(employeeId = employeeId)
    }

    /**
     * Deletes all employees.
     *
     * @return The number of deleted records.
     */
    suspend fun deleteAll(): Int = withContext(Dispatchers.IO) {
        tracer.debug("Deleting all employees.")
        return@withContext employeeRepository.deleteAll()
    }

    /**
     * Retrieves the total count of employees.
     *
     * @return The total count of existing records.
     */
    suspend fun count(): Int = withContext(Dispatchers.IO) {
        return@withContext employeeRepository.count()
    }

    /**
     * Verifies if the employee's fields.
     *
     * @param employeeId The ID of the employee being verified.
     * @param employeeRequest The employee request details.
     * @param reason The reason for the email verification.
     * @throws EmployeeError If any of the fields is invalid.
     */
    private fun verifyIntegrity(employeeId: Uuid?, employeeRequest: EmployeeRequest, reason: String) {
        employeeRequest.contact?.let { contact ->
            val phone: String = contact.phone
            val phoneValidation: IValidator.Result = PhoneValidator.validate(value = phone)
            if (phoneValidation is IValidator.Result.Failure) {
                throw EmployeeError.InvalidPhoneFormat(
                    employeeId = employeeId,
                    phone = phone,
                    reason = "$reason ${phoneValidation.reason}"
                )
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
                throw EmployeeError.InvalidEmailFormat(
                    employeeId = employeeId,
                    email = email,
                    reason = "$reason ${emailValidation.reason}"
                )
            }
        }
    }
}
