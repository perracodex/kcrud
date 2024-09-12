/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.domain.employment.repository

import kcrud.base.persistence.pagination.Page
import kcrud.base.persistence.pagination.Pageable
import kcrud.domain.employment.model.EmploymentDto
import kcrud.domain.employment.model.EmploymentRequest
import kotlin.uuid.Uuid

/**
 * Responsible for managing employment data.
 */
internal interface IEmploymentRepository {

    /**
     * Retrieves all employments.
     *
     * @param pageable The pagination options to be applied, or null for a single all-in-one page.
     * @return List of [EmploymentDto] entries.
     */
    fun findAll(pageable: Pageable? = null): Page<EmploymentDto>

    /**
     * Retrieves an employment by its ID.
     *
     * @param employeeId The ID of the employee associated with the employment.
     * @param employmentId The ID of the employment to be retrieved.
     * @return The resolved [EmploymentDto] if found, null otherwise.
     */
    fun findById(employeeId: Uuid, employmentId: Uuid): EmploymentDto?

    /**
     * Retrieves all employment entries for a given employee.
     *
     * @param employeeId The ID of the employee associated with the employment.
     * @return List of [EmploymentDto] entries.
     */
    fun findByEmployeeId(employeeId: Uuid): List<EmploymentDto>

    /**
     * Creates a new employment.
     *
     * @param employeeId The employee ID associated with the employment.
     * @param employmentRequest The employment to be created.
     * @return The ID of the created employment.
     */
    fun create(employeeId: Uuid, employmentRequest: EmploymentRequest): Uuid

    /**
     * Updates an employment's details.
     *
     * @param employeeId The employee ID associated with the employment.
     * @param employmentId The ID of the employment to be updated.
     * @param employmentRequest The new details for the employment.
     * @return The number of updated records.
     */
    fun update(employeeId: Uuid, employmentId: Uuid, employmentRequest: EmploymentRequest): Int

    /**
     * Deletes an employment using the provided ID.
     *
     * @param employmentId The ID of the employment to be deleted.
     * @return The number of delete records.
     */
    fun delete(employmentId: Uuid): Int

    /**
     * Deletes all an employments for the given employee ID.
     *
     * @param employeeId The ID of the employee to delete all its employments.
     * @return The number of delete records.
     */
    fun deleteAll(employeeId: Uuid): Int

    /**
     * Retrieves the total count of employments.
     *
     * @param employeeId The ID of the employee to count its employments, or null to count all employments.
     * @return The total count of existing records.
     */
    fun count(employeeId: Uuid? = null): Int
}
