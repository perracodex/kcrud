/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.domain.contact.repository

import kcrud.base.persistence.pagination.Page
import kcrud.base.persistence.pagination.Pageable
import kcrud.domain.contact.entity.ContactEntity
import kcrud.domain.contact.entity.ContactRequest
import kcrud.domain.employee.entity.EmployeeRequest
import kotlin.uuid.Uuid

/**
 * Responsible for managing [ContactEntity] data.
 */
internal interface IContactRepository {

    /**
     * Finds a contact by its ID.
     *
     * @param contactId The id of the contact to be retrieved.
     * @return The resolved [ContactEntity] if it exists, null otherwise.
     */
    fun findById(contactId: Uuid): ContactEntity?

    /**
     * Finds a contact by the provided [employeeId].
     *
     * @param employeeId The id of the employee whose contact is to be retrieved.
     * @return The resolve [ContactEntity] if it exists, null otherwise.
     */
    fun findByEmployeeId(employeeId: Uuid): ContactEntity?

    /**
     * Retrieves all contacts.
     *
     * @param pageable The pagination options to be applied, or null for a single all-in-one page.
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
    fun syncWithEmployee(employeeId: Uuid, employeeRequest: EmployeeRequest): Uuid?

    /**
     * Creates a new contact for an employee.
     *
     * @param employeeId The id of the employee to create the contact for.
     * @param contactRequest The details of the contact to be created.
     * @return The id of the created contact.
     */
    fun create(employeeId: Uuid, contactRequest: ContactRequest): Uuid

    /**
     * Updates the contact of an employee.
     *
     * @param contactId The id of the contact to be updated.
     * @param contactRequest The new details for the contact.
     * @return The number of updated records.
     */
    fun update(employeeId: Uuid, contactId: Uuid, contactRequest: ContactRequest): Int

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