/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.domain.employee.repository

import kcrud.base.persistence.pagination.Page
import kcrud.base.persistence.pagination.Pageable
import kcrud.domain.employee.entity.EmployeeDto
import kcrud.domain.employee.entity.EmployeeFilterSet
import kcrud.domain.employee.entity.EmployeeRequest
import kotlin.uuid.Uuid

/**
 * Responsible for managing employee data.
 */
internal interface IEmployeeRepository {

    /**
     * Retrieves an employee by its ID.
     *
     * @param employeeId The ID of the employee to be retrieved.
     * @return The resolved [EmployeeDto] if found, null otherwise.
     */
    fun findById(employeeId: Uuid): EmployeeDto?

    /**
     * Retrieves all employees.
     *
     * @param pageable The pagination options to be applied, or null for a single all-in-one page.
     * @return List of [EmployeeDto] entries.
     */
    fun findAll(pageable: Pageable? = null): Page<EmployeeDto>

    /**
     * Retrieves all employees matching the provided [filterSet].
     *
     * @param filterSet The [EmployeeFilterSet] to be applied.
     * @param pageable The pagination options to be applied, or null for a single all-in-one page.
     * @return List of [EmployeeDto] entries.
     */
    fun search(filterSet: EmployeeFilterSet, pageable: Pageable? = null): Page<EmployeeDto>

    /**
     * Creates a new employee.
     *
     * @param employeeRequest The employee to be created.
     * @return The ID of the created employee.
     */
    fun create(employeeRequest: EmployeeRequest): Uuid

    /**
     * Updates an employee's details.
     *
     * @param employeeId The ID of the employee to be updated.
     * @param employeeRequest The new details for the employee.
     * @return The number of updated records.
     */
    fun update(employeeId: Uuid, employeeRequest: EmployeeRequest): Int

    /**
     * Deletes an employee using the provided ID.
     *
     * @param employeeId The ID of the employee to be deleted.
     * @return The number of delete records.
     */
    fun delete(employeeId: Uuid): Int

    /**
     * Deletes all employees.
     *
     * @return The number of deleted records.
     */
    fun deleteAll(): Int

    /**
     * Retrieves the total count of employees.
     *
     * @return The total count of existing records.
     */
    fun count(): Int
}
