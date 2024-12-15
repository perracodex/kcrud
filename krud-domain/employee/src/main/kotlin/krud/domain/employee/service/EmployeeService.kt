/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package krud.domain.employee.service

import io.perracodex.exposed.pagination.Page
import io.perracodex.exposed.pagination.Pageable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import krud.base.context.SessionContext
import krud.base.env.Tracer
import krud.domain.employee.model.Employee
import krud.domain.employee.model.EmployeeFilterSet
import krud.domain.employee.model.EmployeeRequest
import krud.domain.employee.repository.IEmployeeRepository
import kotlin.uuid.Uuid

/**
 * Employee service, where all the employee business logic should be defined.
 */
public class EmployeeService internal constructor(
    @Suppress("unused") private val sessionContext: SessionContext,
    private val employeeRepository: IEmployeeRepository
) {
    private val tracer: Tracer = Tracer<EmployeeService>()

    /**
     * Retrieves an employee by its ID.
     *
     * @param employeeId The ID of the employee to be retrieved.
     * @return The resolved [Employee] if found, null otherwise.
     */
    public suspend fun findById(employeeId: Uuid): Employee? = withContext(Dispatchers.IO) {
        return@withContext employeeRepository.findById(employeeId = employeeId)
    }

    /**
     * Retrieves all employees.
     *
     * @param pageable The pagination options to be applied, or null for a single all-in-one page.
     * @return List of [Employee] entries.
     */
    public suspend fun findAll(pageable: Pageable? = null): Page<Employee> = withContext(Dispatchers.IO) {
        return@withContext employeeRepository.findAll(pageable = pageable)
    }

    /**
     * Retrieves all employees matching the provided [filterSet].
     *
     * @param filterSet The [EmployeeFilterSet] to be applied.
     * @param pageable The pagination options to be applied, or null for a single all-in-one page.
     * @return List of [Employee] entries.
     */
    public suspend fun filter(filterSet: EmployeeFilterSet, pageable: Pageable? = null): Page<Employee> {
        return withContext(Dispatchers.IO) {
            employeeRepository.filter(filterSet = filterSet, pageable = pageable)
        }
    }

    /**
     * Retrieves all employees matching the provided given [term].
     *
     * @param term The search term to be used. Can be partial.
     * @param pageable The pagination options to be applied, or null for a single all-in-one page.
     * @return List of [Employee] entries.
     */
    public suspend fun search(term: String, pageable: Pageable? = null): Page<Employee> {
        return withContext(Dispatchers.IO) {
            employeeRepository.search(term = term, pageable = pageable)
        }
    }

    /**
     * Creates a new employee.
     *
     * @param request The [EmployeeRequest] to be created.
     * @return A [Result] with the created [Employee], or an error on failure.
     */
    public suspend fun create(request: EmployeeRequest): Result<Employee> {
        tracer.debug("Creating a new employee.")

        return EmployeeConstraints.check(
            employeeId = null,
            request = request,
            reason = "Create Employee.",
            repository = employeeRepository
        ).fold(
            onSuccess = {
                runCatching {
                    withContext(Dispatchers.IO) {
                        employeeRepository.create(request = request)
                    }
                }
            },
            onFailure = { error ->
                tracer.error(message = "Failed to create a new employee.", cause = error)
                Result.failure(error)
            }
        )
    }

    /**
     * Updates an employee's details.
     *
     * @param employeeId The ID of the employee to be updated.
     * @param request The new [EmployeeRequest] to be updated.
     * @return A [Result] with the updated [Employee] or null if such does not exist; or an error on failure.
     */
    public suspend fun update(
        employeeId: Uuid,
        request: EmployeeRequest
    ): Result<Employee?> {
        tracer.debug("Updating employee with ID: $employeeId.")

        return EmployeeConstraints.check(
            employeeId = employeeId,
            request = request,
            reason = "Update Employee.",
            repository = employeeRepository
        ).fold(
            onSuccess = {
                runCatching {
                    withContext(Dispatchers.IO) {
                        employeeRepository.update(employeeId = employeeId, request = request)
                    }
                }
            },
            onFailure = { error ->
                tracer.error(message = "Failed to update employee.", cause = error)
                Result.failure(error)
            }
        )
    }

    /**
     * Deletes an employee using the provided ID.
     *
     * @param employeeId The ID of the employee to be deleted.
     * @return The number of delete records.
     */
    public suspend fun delete(employeeId: Uuid): Int = withContext(Dispatchers.IO) {
        tracer.debug("Deleting employee with ID: $employeeId.")
        return@withContext employeeRepository.delete(employeeId = employeeId)
    }

    /**
     * Deletes all employees.
     *
     * @return The number of deleted records.
     */
    public suspend fun deleteAll(): Int = withContext(Dispatchers.IO) {
        tracer.debug("Deleting all employees.")
        return@withContext employeeRepository.deleteAll()
    }

    /**
     * Retrieves the total count of employees.
     *
     * @return The total count of existing records.
     */
    public suspend fun count(): Int = withContext(Dispatchers.IO) {
        return@withContext employeeRepository.count()
    }
}
