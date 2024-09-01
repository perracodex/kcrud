/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.domain.contact.repository

import kcrud.base.database.schema.contact.ContactTable
import kcrud.base.database.service.transactionWithSchema
import kcrud.base.env.SessionContext
import kcrud.base.persistence.pagination.Page
import kcrud.base.persistence.pagination.Pageable
import kcrud.base.persistence.pagination.paginate
import kcrud.domain.contact.entity.ContactEntity
import kcrud.domain.contact.entity.ContactRequest
import kcrud.domain.employee.entity.EmployeeRequest
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.update
import kotlin.uuid.Uuid

/**
 * Implementation of [IContactRepository].
 * Responsible for managing [ContactEntity] data.
 */
internal class ContactRepository(
    private val sessionContext: SessionContext
) : IContactRepository {

    override fun findById(contactId: Uuid): ContactEntity? {
        return transactionWithSchema(schema = sessionContext.schema) {
            ContactTable.selectAll().where {
                ContactTable.id eq contactId
            }.singleOrNull()?.let { resultRow ->
                ContactEntity.from(row = resultRow)
            }
        }
    }

    override fun findByEmployeeId(employeeId: Uuid): ContactEntity? {
        return transactionWithSchema(schema = sessionContext.schema) {
            ContactTable.selectAll().where {
                ContactTable.employeeId eq employeeId
            }.singleOrNull()?.let { resultRow ->
                ContactEntity.from(row = resultRow)
            }
        }
    }

    override fun findAll(pageable: Pageable?): Page<ContactEntity> {
        return transactionWithSchema(schema = sessionContext.schema) {
            val totalElements: Int = ContactTable.selectAll().count().toInt()

            val content: List<ContactEntity> = ContactTable
                .selectAll()
                .paginate(pageable = pageable)
                .map { resultRow ->
                    ContactEntity.from(row = resultRow)
                }

            Page.build(
                content = content,
                totalElements = totalElements,
                pageable = pageable
            )
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
                    contactRequest = employeeRequest.contact
                )

                newContactId.takeIf { updateCount > 0 }
            } ?: create(employeeId = employeeId, contactRequest = employeeRequest.contact)
        } ?: run {
            // If the request does not contain a contact, delete the existing one.
            deleteByEmployeeId(employeeId = employeeId)
            null
        }
    }

    override fun create(employeeId: Uuid, contactRequest: ContactRequest): Uuid {
        return transactionWithSchema(schema = sessionContext.schema) {
            ContactTable.insert { contactRow ->
                contactRow.mapContactRequest(
                    employeeId = employeeId,
                    request = contactRequest
                )
            } get ContactTable.id
        }
    }

    override fun update(employeeId: Uuid, contactId: Uuid, contactRequest: ContactRequest): Int {
        return transactionWithSchema(schema = sessionContext.schema) {
            ContactTable.update(
                where = {
                    ContactTable.id eq contactId
                }
            ) { contactRow ->
                contactRow.mapContactRequest(
                    employeeId = employeeId,
                    request = contactRequest
                )
            }
        }
    }

    override fun delete(contactId: Uuid): Int {
        return transactionWithSchema(schema = sessionContext.schema) {
            ContactTable.deleteWhere {
                id eq contactId
            }
        }
    }

    override fun deleteByEmployeeId(employeeId: Uuid): Int {
        return transactionWithSchema(schema = sessionContext.schema) {
            ContactTable.deleteWhere {
                ContactTable.employeeId eq employeeId
            }
        }
    }

    override fun count(employeeId: Uuid?): Int {
        return transactionWithSchema(schema = sessionContext.schema) {
            employeeId?.let { id ->
                ContactTable.select(column = ContactTable.employeeId eq id).count().toInt()
            } ?: ContactTable.selectAll().count().toInt()
        }
    }

    /**
     * Populates an SQL [UpdateBuilder] with data from a [ContactRequest] instance,
     * so that it can be used to update or create a database record.
     */
    private fun UpdateBuilder<Int>.mapContactRequest(employeeId: Uuid, request: ContactRequest) {
        this[ContactTable.employeeId] = employeeId
        this[ContactTable.email] = request.email.trim()
        this[ContactTable.phone] = request.phone.trim()
    }
}
