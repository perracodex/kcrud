/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.domain.contact.repository

import io.perracodex.exposed.pagination.Page
import io.perracodex.exposed.pagination.Pageable
import io.perracodex.exposed.pagination.paginate
import kcrud.core.context.SessionContext
import kcrud.database.schema.contact.ContactTable
import kcrud.database.util.transaction
import kcrud.domain.contact.model.Contact
import kcrud.domain.contact.model.ContactRequest
import kcrud.domain.employee.model.EmployeeRequest
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.update
import kotlin.uuid.Uuid

/**
 * Implementation of [IContactRepository].
 * Responsible for managing [Contact] data.
 */
internal class ContactRepository(
    private val sessionContext: SessionContext
) : IContactRepository {

    override fun findById(contactId: Uuid): Contact? {
        return transaction(sessionContext = sessionContext) {
            ContactTable.selectAll().where {
                ContactTable.id eq contactId
            }.singleOrNull()?.let { resultRow ->
                Contact.from(row = resultRow)
            }
        }
    }

    override fun findByEmployeeId(employeeId: Uuid): Contact? {
        return transaction(sessionContext = sessionContext) {
            ContactTable.selectAll().where {
                ContactTable.employeeId eq employeeId
            }.singleOrNull()?.let { resultRow ->
                Contact.from(row = resultRow)
            }
        }
    }

    override fun findAll(pageable: Pageable?): Page<Contact> {
        return transaction(sessionContext = sessionContext) {
            ContactTable.selectAll().paginate(pageable = pageable, map = Contact)
        }
    }

    override fun syncWithEmployee(employeeId: Uuid, employeeRequest: EmployeeRequest): Uuid? {
        return employeeRequest.contact?.let {
            val contactId: Uuid? = findByEmployeeId(employeeId = employeeId)?.id

            // If the contact already exists, update it, otherwise create it.
            contactId?.let { newContactId ->
                val updateCount = update(
                    employeeId = employeeId,
                    contactId = contactId,
                    request = employeeRequest.contact
                )

                newContactId.takeIf { updateCount > 0 }
            } ?: create(employeeId = employeeId, request = employeeRequest.contact)
        } ?: run {
            // If the request does not contain a contact, delete any existing one.
            deleteByEmployeeId(employeeId = employeeId)
            null
        }
    }

    override fun create(employeeId: Uuid, request: ContactRequest): Uuid {
        return transaction(sessionContext = sessionContext) {
            ContactTable.insert { statement ->
                statement.toStatement(
                    employeeId = employeeId,
                    request = request
                )
            } get ContactTable.id
        }
    }

    override fun update(employeeId: Uuid, contactId: Uuid, request: ContactRequest): Int {
        return transaction(sessionContext = sessionContext) {
            ContactTable.update(
                where = {
                    ContactTable.id eq contactId
                }
            ) { statement ->
                statement.toStatement(
                    employeeId = employeeId,
                    request = request
                )
            }
        }
    }

    override fun delete(contactId: Uuid): Int {
        return transaction(sessionContext = sessionContext) {
            ContactTable.deleteWhere {
                id eq contactId
            }
        }
    }

    override fun deleteByEmployeeId(employeeId: Uuid): Int {
        return transaction(sessionContext = sessionContext) {
            ContactTable.deleteWhere {
                ContactTable.employeeId eq employeeId
            }
        }
    }

    override fun count(employeeId: Uuid?): Int {
        return transaction(sessionContext = sessionContext) {
            ContactTable.selectAll().apply {
                employeeId?.let { id ->
                    where { ContactTable.employeeId eq id }
                }
            }.count().toInt()
        }
    }

    /**
     * Populates an SQL [UpdateBuilder] with data from a [ContactRequest] instance,
     * so that it can be used to update or create a database record.
     */
    private fun UpdateBuilder<Int>.toStatement(employeeId: Uuid, request: ContactRequest) {
        this[ContactTable.employeeId] = employeeId
        this[ContactTable.email] = request.email.trim()
        this[ContactTable.phone] = request.phone.trim()
    }
}
