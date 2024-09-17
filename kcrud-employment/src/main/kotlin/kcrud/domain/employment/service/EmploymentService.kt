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
     * @param request The [EmploymentRequest] to be created.
     * @return A [Result] with the updated [Employment] or null if such does not exist; or an error on failure.
     */
    public suspend fun create(
        employeeId: Uuid,
        request: EmploymentRequest
    ): Result<Employment> {
        tracer.debug("Creating employment for employee with ID: $employeeId")

        return verify(
            employeeId = employeeId,
            employmentId = null,
            employmentRequest = request,
            reason = "Create Employment."
        ).fold(
            onSuccess = {
                runCatching {
                    withContext(Dispatchers.IO) {
                        employmentRepository.create(
                            employeeId = employeeId,
                            request = request
                        )
                    }
                }
            },
            onFailure = { error ->
                tracer.error(message = "Failed to create a new employment.", cause = error)
                return Result.failure(error)
            }
        )
    }

    /**
     * Updates an employment's details.
     *
     * @param employeeId The employee ID associated with the employment.
     * @param employmentId The ID of the employment to be updated.
     * @param request The new [EmploymentRequest] details.
     * @return A [Result] with the updated [Employment] or null if such does not exist; or an error on failure.
     */
    public suspend fun update(
        employeeId: Uuid,
        employmentId: Uuid,
        request: EmploymentRequest
    ): Result<Employment?> {
        tracer.debug("Updating employment with ID: $employmentId")

        return verify(
            employeeId = employeeId,
            employmentId = employmentId,
            employmentRequest = request,
            reason = "Update Employment."
        ).fold(
            onSuccess = {
                runCatching {
                    withContext(Dispatchers.IO) {
                        employmentRepository.update(
                            employeeId = employeeId,
                            employmentId = employmentId,
                            request = request
                        )
                    }
                }
            },
            onFailure = { error ->
                tracer.error(message = "Failed to update employment.", cause = error)
                return Result.failure(error)
            }
        )
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

    /**
     * Verifies the integrity of the employment request.
     *
     * @param employeeId The ID of the employee associated with the employment.
     * @param employmentId The ID of the employment to be verified.
     * @param employmentRequest The [EmploymentRequest] details to be verified.
     * @param reason The reason for the verification. To be included in the error message.
     * @return A [Result] with verification state.
     */
    private fun verify(
        employeeId: Uuid,
        employmentId: Uuid?,
        employmentRequest: EmploymentRequest,
        reason: String
    ): Result<Unit> {

        // Verify that the employment period dates are valid.
        employmentRequest.period.endDate?.let { endDate ->
            if (endDate < employmentRequest.period.startDate) {
                return Result.failure(
                    EmploymentError.PeriodDatesMismatch(
                        employeeId = employeeId,
                        employmentId = employmentId,
                        startDate = employmentRequest.period.startDate,
                        endDate = endDate,
                        reason = reason
                    )
                )
            }
        }

        // Verify that the employment probation end date is valid.
        employmentRequest.probationEndDate?.let { probationEndDate ->
            if (probationEndDate < employmentRequest.period.startDate) {
                return Result.failure(
                    EmploymentError.InvalidProbationEndDate(
                        employeeId = employeeId,
                        employmentId = employmentId,
                        startDate = employmentRequest.period.startDate,
                        probationEndDate = probationEndDate,
                        reason = reason
                    )
                )
            }
        }

        return Result.success(Unit)
    }
}
