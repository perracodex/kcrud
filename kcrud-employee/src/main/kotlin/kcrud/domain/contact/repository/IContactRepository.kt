/*
 * Copyright (c) 2024-Present Perraco. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.domain.contact.repository

import kcrud.base.persistence.pagination.Page
import kcrud.base.persistence.pagination.Pageable
import kcrud.domain.contact.entity.ContactEntity
import kcrud.domain.contact.entity.ContactRequest
import kcrud.domain.employee.entity.EmployeeRequest
import java.util.*

interface IContactRepository {

    /**
     * Finds a contact by its ID.
     *
     * @param contactId The id of the contact to be retrieved.
     * @return The resolved [ContactEntity] if it exists, null otherwise.
     */
    fun findById(contactId: UUID): ContactEntity?

    /**
     * Finds a contact by the provided [employeeId].
     *
     * @param employeeId The id of the employee whose contact is to be retrieved.
     * @return The resolve [ContactEntity] if it exists, null otherwise.
     */
    fun findByEmployeeId(employeeId: UUID): ContactEntity?

    /**
     * Retrieves all contacts.
     *
     * @param pageable The pagination options to be applied.
     *                 If not provided, a single page with the result will be returned.
     * @return List of [ContactEntity] entries.
     */
    fun findAll(pageable: Pageable? = null): Page<ContactEntity>

    /**
     * Sets the contact of an employee, either by creating it, updating it
     * or deleting it, accordingly to the [employeeRequest].
     *
     * @param employeeId The id of the employee to set the contact for.
     * @param employeeRequest The details of the employee to be processed.
     * @return The id of the contact if it was created or updated, null if it was deleted.
     */
    fun syncWithEmployee(employeeId: UUID, employeeRequest: EmployeeRequest): UUID?

    /**
     * Creates a new contact for an employee.
     *
     * @param employeeId The id of the employee to create the contact for.
     * @param contactRequest The details of the contact to be created.
     * @return The id of the created contact.
     */
    fun create(employeeId: UUID, contactRequest: ContactRequest): UUID

    /**
     * Updates the contact of an employee.
     *
     * @param contactId The id of the contact to be updated.
     * @param contactRequest The new details for the contact.
     * @return The number of updated records.
     */
    fun update(employeeId: UUID, contactId: UUID, contactRequest: ContactRequest): Int

    /**
     * Deletes the contact of an employee.
     *
     * @param contactId The id of the contact to be deleted.
     * @return The number of deleted records.
     */
    fun delete(contactId: UUID): Int

    /**
     * Deletes the contact of an employee.
     *
     * @param employeeId The id of the employee whose contact is to be deleted.
     * @return The number of deleted records.
     */
    fun deleteByEmployeeId(employeeId: UUID): Int

    /**
     * Retrieves the total count of contacts.
     *
     * @param employeeId The ID of the employee to count its contacts, or null to count all contacts.
     * @return The total count of existing records.
     */
    fun count(employeeId: UUID? = null): Int
}