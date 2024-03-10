/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.domain.employment.repository

import kcrud.base.persistence.pagination.Page
import kcrud.base.persistence.pagination.Pageable
import kcrud.domain.employment.entities.EmploymentEntity
import kcrud.domain.employment.entities.EmploymentRequest
import java.util.*

interface IEmploymentRepository {

    /**
     * Retrieves all employments.
     *
     * @param pageable The pagination options to be applied.
     *                 If not provided, a single page with the result will be returned.
     * @return List of [EmploymentEntity] entities.
     */
    fun findAll(pageable: Pageable? = null): Page<EmploymentEntity>

    /**
     * Retrieves an employment by its ID.
     *
     * @param employeeId The ID of the employee associated with the employment.
     * @param employmentId The ID of the employment to be retrieved.
     * @return The resolved [EmploymentEntity] if found, null otherwise.
     */
    fun findById(employeeId: UUID, employmentId: UUID): EmploymentEntity?

    /**
     * Retrieves all employment entities for a given employee.
     *
     * @param employeeId The ID of the employee associated with the employment.
     * @return List of [EmploymentEntity] entities.
     */
    fun findByEmployeeId(employeeId: UUID): List<EmploymentEntity>

    /**
     * Creates a new employment.
     *
     * @param employeeId The employee ID associated with the employment.
     * @param employmentRequest The employment to be created.
     * @return The ID of the created employment.
     */
    fun create(employeeId: UUID, employmentRequest: EmploymentRequest): UUID

    /**
     * Updates an employment's details.
     *
     * @param employeeId The employee ID associated with the employment.
     * @param employmentId The ID of the employment to be updated.
     * @param employmentRequest The new details for the employment.
     * @return The number of updated records.
     */
    fun update(employeeId: UUID, employmentId: UUID, employmentRequest: EmploymentRequest): Int

    /**
     * Deletes an employment using the provided ID.
     *
     * @param employmentId The ID of the employment to be deleted.
     * @return The number of delete records.
     */
    fun delete(employmentId: UUID): Int

    /**
     * Deletes all an employments for the given employee ID.
     *
     * @param employeeId The ID of the employee to delete all its employments.
     * @return The number of delete records.
     */
    fun deleteAll(employeeId: UUID): Int

    /**
     * Retrieves the total count of employments.
     *
     * @param employeeId The ID of the employee to count its employments, or null to count all employments.
     * @return The total count of existing records.
     */
    fun count(employeeId: UUID? = null): Int
}
