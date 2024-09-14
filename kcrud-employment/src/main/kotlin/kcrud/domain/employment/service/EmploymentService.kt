/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.domain.employment.service

import kcrud.base.env.SessionContext
import kcrud.base.env.Tracer
import kcrud.base.persistence.pagination.Page
import kcrud.base.persistence.pagination.Pageable
import kcrud.domain.employment.errors.EmploymentError
import kcrud.domain.employment.model.Employment
import kcrud.domain.employment.model.EmploymentRequest
import kcrud.domain.employment.repository.IEmploymentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.uuid.Uuid

/**
 * Employment service, where all the employment business logic should be defined.
 */
public class EmploymentService internal constructor(
    @Suppress("unused") private val sessionContext: SessionContext,
    private val employmentRepository: IEmploymentRepository
) {
    private val tracer = Tracer<EmploymentService>()

    /**
     * Retrieves all employments.
     *
     * @param pageable The pagination options to be applied, or null for a single all-in-one page.
     * @return List of [Employment] entries.
     */
    public suspend fun findAll(pageable: Pageable? = null): Page<Employment> = withContext(Dispatchers.IO) {
        return@withContext employmentRepository.findAll(pageable = pageable)
    }

    /**
     * Retrieves an employment by its ID.
     *
     * @param employeeId The ID of the employee associated with the employment.
     * @param employmentId The ID of the employment to be retrieved.
     * @return The resolved [Employment] if found, null otherwise.
     */
    public suspend fun findById(employeeId: Uuid, employmentId: Uuid): Employment? = withContext(Dispatchers.IO) {
        return@withContext employmentRepository.findById(employeeId = employeeId, employmentId = employmentId)
    }

    /**
     * Retrieves an employment by its ID or throws an exception if not found.
     *
     * @param employeeId The ID of the employee associated with the employment.
     * @param employmentId The ID of the employment to be retrieved.
     * @return The resolved [Employment].
     * @throws EmploymentError.EmploymentNotFound if the employment doesn't exist.
     */
    public suspend fun findByIdOrThrow(employeeId: Uuid, employmentId: Uuid): Employment = withContext(Dispatchers.IO) {
        return@withContext employmentRepository.findByIdOrThrow(employeeId = employeeId, employmentId = employmentId)
    }

    /**
     * Retrieves all employment entries for a given employee.
     *
     * @param employeeId The ID of the employee associated with the employment.
     * @return List of [Employment] entries.
     */
    public suspend fun findByEmployeeId(employeeId: Uuid): List<Employment> = withContext(Dispatchers.IO) {
        return@withContext employmentRepository.findByEmployeeId(employeeId = employeeId)
    }

    /**
     * Creates a new employment.
     *
     * @param employeeId The employee ID associated with the employment.
     * @param employmentRequest The employment to be created.
     * @return The ID of the created employment.
     */
    public suspend fun create(
        employeeId: Uuid,
        employmentRequest: EmploymentRequest
    ): Employment {
        tracer.debug("Creating employment for employee with ID: $employeeId")

        verify(
            employeeId = employeeId,
            employmentId = null,
            employmentRequest = employmentRequest,
            reason = "Create Employment."
        )

        return withContext(Dispatchers.IO) {
            return@withContext employmentRepository.createAndGet(
                employeeId = employeeId,
                employmentRequest = employmentRequest
            )
        }
    }

    /**
     * Updates an employment's details.
     *
     * @param employeeId The employee ID associated with the employment.
     * @param employmentId The ID of the employment to be updated.
     * @param employmentRequest The new details for the employment.
     * @return The updated [Employment].
     */
    public suspend fun update(
        employeeId: Uuid,
        employmentId: Uuid,
        employmentRequest: EmploymentRequest
    ): Employment {
        tracer.debug("Updating employment with ID: $employmentId")

        verify(
            employeeId = employeeId,
            employmentId = employmentId,
            employmentRequest = employmentRequest,
            reason = "Update Employment."
        )

        return withContext(Dispatchers.IO) {
            return@withContext employmentRepository.updateAndGet(
                employeeId = employeeId,
                employmentId = employmentId,
                employmentRequest = employmentRequest
            )
        }
    }

    /**
     * Deletes an employment using the provided ID.
     *
     * @param employmentId The ID of the employment to be deleted.
     * @return The number of delete records.
     */
    public suspend fun delete(employmentId: Uuid): Int = withContext(Dispatchers.IO) {
        tracer.debug("Deleting employment with ID: $employmentId")
        return@withContext employmentRepository.delete(employmentId = employmentId)
    }

    /**
     * Deletes all an employments for the given employee ID.
     *
     * @param employeeId The ID of the employee to delete all its employments.
     * @return The number of delete records.
     */
    public suspend fun deleteAll(employeeId: Uuid): Int = withContext(Dispatchers.IO) {
        tracer.debug("Deleting all employments for employee with ID: $employeeId")
        return@withContext employmentRepository.deleteAll(employeeId = employeeId)
    }

    private fun verify(employeeId: Uuid, employmentId: Uuid?, employmentRequest: EmploymentRequest, reason: String) {
        // Verify that the employment period dates are valid.
        employmentRequest.period.endDate?.let { endDate ->
            if (endDate < employmentRequest.period.startDate) {
                throw EmploymentError.PeriodDatesMismatch(
                    employeeId = employeeId,
                    employmentId = employmentId,
                    startDate = employmentRequest.period.startDate,
                    endDate = endDate,
                    reason = reason
                )
            }
        }

        // Verify that the employment probation end date is valid.
        employmentRequest.probationEndDate?.let { probationEndDate ->
            if (probationEndDate < employmentRequest.period.startDate) {
                throw EmploymentError.InvalidProbationEndDate(
                    employeeId = employeeId,
                    employmentId = employmentId,
                    startDate = employmentRequest.period.startDate,
                    probationEndDate = probationEndDate,
                    reason = reason
                )
            }
        }
    }
}
