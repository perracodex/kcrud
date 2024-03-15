/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.domain.employee.repository

import kcrud.base.persistence.pagination.Page
import kcrud.base.persistence.pagination.Pageable
import kcrud.domain.employee.entity.EmployeeEntity
import kcrud.domain.employee.entity.EmployeeFilterSet
import kcrud.domain.employee.entity.EmployeeRequest
import java.util.*

interface IEmployeeRepository {

    /**
     * Retrieves an employee by its ID.
     *
     * @param employeeId The ID of the employee to be retrieved.
     * @return The resolved [EmployeeEntity] if found, null otherwise.
     */
    fun findById(employeeId: UUID): EmployeeEntity?

    /**
     * Retrieves all employee entities.
     *
     * @param pageable The pagination options to be applied.
     *                 If not provided, a single page with the result will be returned.
     * @return List of [EmployeeEntity] entries.
     */
    fun findAll(pageable: Pageable? = null): Page<EmployeeEntity>

    /**
     * Retrieves all employee entities matching the provided [filterSet].
     *
     * @param filterSet The [EmployeeFilterSet] to be applied.
     * @return List of [EmployeeEntity] entries.
     */
    fun filter(filterSet: EmployeeFilterSet): Page<EmployeeEntity>

    /**
     * Creates a new employee.
     *
     * @param employeeRequest The employee to be created.
     * @return The ID of the created employee.
     */
    fun create(employeeRequest: EmployeeRequest): UUID

    /**
     * Updates an employee's details.
     *
     * @param employeeId The ID of the employee to be updated.
     * @param employeeRequest The new details for the employee.
     * @return The number of updated records.
     */
    fun update(employeeId: UUID, employeeRequest: EmployeeRequest): Int

    /**
     * Deletes an employee using the provided ID.
     *
     * @param employeeId The ID of the employee to be deleted.
     * @return The number of delete records.
     */
    fun delete(employeeId: UUID): Int

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

