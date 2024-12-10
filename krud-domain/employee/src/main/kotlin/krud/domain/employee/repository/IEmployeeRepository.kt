/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package krud.domain.employee.repository

import io.perracodex.exposed.pagination.Page
import io.perracodex.exposed.pagination.Pageable
import krud.domain.employee.model.Employee
import krud.domain.employee.model.EmployeeFilterSet
import krud.domain.employee.model.EmployeeRequest
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
     * Retrieves all employees.
     *
     * @param pageable The pagination options to be applied, or null for a single all-in-one page.
     * @return List of [Employee] entries.
     */
    fun findAll(pageable: Pageable? = null): Page<Employee>

    /**
     * Retrieves an employee by the given [workEmail].
     *
     * Optionally, an [excludeEmployeeId] can be provided to exclude a specific employee
     * from the search, which is useful when updating an employee's email.
     *
     * @param workEmail The work email of the employee to be retrieved.
     * @param excludeEmployeeId Optional ID of the employee to be excluded from the search.
     */
    fun findByWorkEmail(workEmail: String, excludeEmployeeId: Uuid?): Employee?

    /**
     * Retrieves all employees matching the provided [filterSet].
     *
     * @param filterSet The [EmployeeFilterSet] to be applied.
     * @param pageable The pagination options to be applied, or null for a single all-in-one page.
     * @return List of [Employee] entries.
     */
    fun filter(filterSet: EmployeeFilterSet, pageable: Pageable? = null): Page<Employee>

    /**
     * Retrieves all employees matching the provided given [term].
     *
     * @param term The search term to be used. Can be partial.
     * @param pageable The pagination options to be applied, or null for a single all-in-one page.
     * @return List of [Employee] entries.
     */
    fun search(term: String, pageable: Pageable? = null): Page<Employee>

    /**
     * Creates a new employee.
     *
     * @param request The employee to be created.
     * @return The newly created Employee.
     */
    fun create(request: EmployeeRequest): Employee

    /**
     * Updates an employee's details.
     *
     * @param employeeId The ID of the employee to be updated.
     * @param request The new details for the employee.
     * @return The updated [Employee], or null if the employee was not found.
     */
    fun update(employeeId: Uuid, request: EmployeeRequest): Employee?

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
