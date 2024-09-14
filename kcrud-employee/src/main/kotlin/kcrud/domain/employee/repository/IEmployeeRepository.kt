/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.domain.employee.repository

import kcrud.base.persistence.pagination.Page
import kcrud.base.persistence.pagination.Pageable
import kcrud.domain.employee.errors.EmployeeError
import kcrud.domain.employee.model.Employee
import kcrud.domain.employee.model.EmployeeFilterSet
import kcrud.domain.employee.model.EmployeeRequest
import kotlin.uuid.Uuid

/**
 * Responsible for managing employee data.
 */
internal interface IEmployeeRepository {

    /**
     * Retrieves an employee by its ID.
     *
     * @param employeeId The ID of the employee to be retrieved.
     * @return The resolved [Employee] if found, null otherwise.
     */
    fun findById(employeeId: Uuid): Employee?

    /**
     * Retrieves an employee by its ID,
     * or throws an exception if the employee is not found.
     *
     * @param employeeId The ID of the employee to be retrieved.
     * @return The resolved [Employee] if found, null otherwise.
     * @throws EmployeeError.EmployeeNotFound If the employee is not found.
     */
    fun findByIdOrThrow(employeeId: Uuid): Employee

    /**
     * Retrieves all employees.
     *
     * @param pageable The pagination options to be applied, or null for a single all-in-one page.
     * @return List of [Employee] entries.
     */
    fun findAll(pageable: Pageable? = null): Page<Employee>

    /**
     * Retrieves all employees matching the provided [filterSet].
     *
     * @param filterSet The [EmployeeFilterSet] to be applied.
     * @param pageable The pagination options to be applied, or null for a single all-in-one page.
     * @return List of [Employee] entries.
     */
    fun search(filterSet: EmployeeFilterSet, pageable: Pageable? = null): Page<Employee>

    /**
     * Creates a new employee.
     *
     * @param employeeRequest The employee to be created.
     * @return The ID of the created employee.
     */
    fun create(employeeRequest: EmployeeRequest): Uuid

    /**
     * Creates a new employee and returns it.
     *
     * @param employeeRequest The employee to be created.
     * @return The newly created [Employee].
     */
    fun createAndGet(employeeRequest: EmployeeRequest): Employee

    /**
     * Updates an employee's details.
     *
     * @param employeeId The ID of the employee to be updated.
     * @param employeeRequest The new details for the employee.
     * @return The number of updated records.
     */
    fun update(employeeId: Uuid, employeeRequest: EmployeeRequest): Int

    /**
     * Updates an employee's details and returns it.
     *
     * @param employeeId The ID of the employee to be updated.
     * @param employeeRequest The new details for the employee.
     * @return The updated [Employee].
     */
    fun updateAndGet(employeeId: Uuid, employeeRequest: EmployeeRequest): Employee

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
