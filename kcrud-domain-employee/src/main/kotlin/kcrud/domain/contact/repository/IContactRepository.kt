/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.domain.contact.repository

import io.perracodex.exposed.pagination.Page
import io.perracodex.exposed.pagination.Pageable
import kcrud.domain.contact.model.Contact
import kcrud.domain.contact.model.ContactRequest
import kcrud.domain.employee.model.EmployeeRequest
import kotlin.uuid.Uuid

/**
 * Responsible for managing [Contact] data.
 */
internal interface IContactRepository {

    /**
     * Finds a contact by its ID.
     *
     * @param contactId The id of the contact to be retrieved.
     * @return The resolved [Contact] if it exists, null otherwise.
     */
    fun findById(contactId: Uuid): Contact?

    /**
     * Finds a contact by the provided [employeeId].
     *
     * @param employeeId The id of the employee whose contact is to be retrieved.
     * @return The resolve [Contact] if it exists, null otherwise.
     */
    fun findByEmployeeId(employeeId: Uuid): Contact?

    /**
     * Retrieves all contacts.
     *
     * @param pageable The pagination options to be applied, or null for a single all-in-one page.
     * @return List of [Contact] entries.
     */
    fun findAll(pageable: Pageable? = null): Page<Contact>

    /**
     * Sets the contact of an employee, either by creating it, updating it
     * or deleting it, accordingly to the [employeeRequest].
     *
     * @param employeeId The id of the employee to set the contact for.
     * @param employeeRequest The details of the employee to be processed.
     * @return The id of the contact if it was created or updated, null if it was deleted.
     */
    fun syncWithEmployee(employeeId: Uuid, employeeRequest: EmployeeRequest): Uuid?

    /**
     * Creates a new contact for an employee.
     *
     * @param employeeId The id of the employee to create the contact for.
     * @param request The details of the contact to be created.
     * @return The id of the created contact.
     */
    fun create(employeeId: Uuid, request: ContactRequest): Uuid

    /**
     * Updates the contact of an employee.
     *
     * @param contactId The id of the contact to be updated.
     * @param request The new details for the contact.
     * @return The number of updated records.
     */
    fun update(employeeId: Uuid, contactId: Uuid, request: ContactRequest): Int

    /**
     * Deletes the contact of an employee.
     *
     * @param contactId The id of the contact to be deleted.
     * @return The number of deleted records.
     */
    fun delete(contactId: Uuid): Int

    /**
     * Deletes the contact of an employee.
     *
     * @param employeeId The id of the employee whose contact is to be deleted.
     * @return The number of deleted records.
     */
    fun deleteByEmployeeId(employeeId: Uuid): Int

    /**
     * Retrieves the total count of contacts.
     *
     * @param employeeId The ID of the employee to count its contacts, or null to count all contacts.
     * @return The total count of existing records.
     */
    fun count(employeeId: Uuid? = null): Int
}
