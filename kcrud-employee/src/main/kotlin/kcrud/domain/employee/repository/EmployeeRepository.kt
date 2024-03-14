/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.domain.employee.repository

import kcrud.access.system.SessionContext
import kcrud.base.database.schema.contact.ContactTable
import kcrud.base.database.schema.employee.EmployeeTable
import kcrud.base.database.service.transactionWithSchema
import kcrud.base.infrastructure.utils.DateTimeUtils
import kcrud.base.persistence.pagination.Page
import kcrud.base.persistence.pagination.Pageable
import kcrud.base.persistence.pagination.applyPagination
import kcrud.domain.contact.repository.IContactRepository
import kcrud.domain.employee.entities.EmployeeEntity
import kcrud.domain.employee.entities.EmployeeFilterSet
import kcrud.domain.employee.entities.EmployeeRequest
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import java.util.*

internal class EmployeeRepository(
    private val sessionContext: SessionContext,
    private val contactRepository: IContactRepository
) : IEmployeeRepository {

    override fun findById(employeeId: UUID): EmployeeEntity? {
        return transactionWithSchema(schema = sessionContext.schema) {
            EmployeeTable.join(
                otherTable = ContactTable,
                joinType = JoinType.LEFT,
                onColumn = EmployeeTable.id,
                otherColumn = ContactTable.employeeId
            ).selectAll().where {
                EmployeeTable.id eq employeeId
            }.singleOrNull()?.let { resultRow ->
                EmployeeEntity.from(row = resultRow)
            }
        }
    }

    override fun findAll(pageable: Pageable?): Page<EmployeeEntity> {
        return transactionWithSchema(schema = sessionContext.schema) {
            val totalElements: Int = EmployeeTable.selectAll().count().toInt()

            val content: List<EmployeeEntity> = EmployeeTable.join(
                otherTable = ContactTable,
                joinType = JoinType.LEFT,
                onColumn = EmployeeTable.id,
                otherColumn = ContactTable.employeeId
            ).selectAll()
                .applyPagination(pageable = pageable)
                .map { resultRow ->
                    EmployeeEntity.from(row = resultRow)
                }

            Page.build(
                content = content,
                totalElements = totalElements,
                pageable = pageable
            )
        }
    }

    override fun filter(filterSet: EmployeeFilterSet): Page<EmployeeEntity> {
        return transactionWithSchema(schema = sessionContext.schema) {
            // Start with a base query selecting all from EmployeeTable.
            val query: Query = EmployeeTable.selectAll()

            // Apply filters dynamically based on the presence of criteria in filterSet.
            // Using lowerCase() to make the search case-insensitive.
            // This could be removed if the database is configured to use a case-insensitive collation.
            filterSet.firstName?.let { firstName ->
                query.andWhere {
                    EmployeeTable.firstName.lowerCase() like "%${firstName.lowercase()}%"
                }
            }
            filterSet.lastName?.let { lastName ->
                query.andWhere {
                    EmployeeTable.lastName.lowerCase() like "%${lastName.lowercase()}%"
                }
            }
            filterSet.honorific?.let { honorificList ->
                if (honorificList.isNotEmpty()) {
                    query.andWhere {
                        EmployeeTable.honorific inList honorificList
                    }
                }
            }
            filterSet.maritalStatus?.let { maritalStatusList ->
                if (maritalStatusList.isNotEmpty()) {
                    query.andWhere {
                        EmployeeTable.maritalStatus inList maritalStatusList
                    }
                }
            }

            // Count total elements after applying filters.
            val totalFilteredElements: Int = query.count().toInt()

            val content: List<EmployeeEntity> = query
                .applyPagination(pageable = filterSet.pageable)
                .map { resultRow ->
                    EmployeeEntity.from(row = resultRow)
                }

            Page.build(
                content = content,
                totalElements = totalFilteredElements,
                pageable = filterSet.pageable
            )
        }
    }

    override fun create(employeeRequest: EmployeeRequest): UUID {
        return transactionWithSchema(schema = sessionContext.schema) {
            val newEmployeeId: UUID = EmployeeTable.insert { employeeRow ->
                employeeRow.mapEmployeeRequest(employeeRequest = employeeRequest, withTimestamp = false)
            } get EmployeeTable.id

            employeeRequest.contact?.let {
                contactRepository.create(
                    employeeId = newEmployeeId,
                    contactRequest = employeeRequest.contact
                )
            }

            newEmployeeId
        }
    }

    override fun update(employeeId: UUID, employeeRequest: EmployeeRequest): Int {
        return transactionWithSchema(schema = sessionContext.schema) {
            val updateCount: Int = EmployeeTable.update(
                where = {
                    EmployeeTable.id eq employeeId
                }
            ) { employeeRow ->
                employeeRow.mapEmployeeRequest(employeeRequest = employeeRequest)
            }

            if (updateCount > 0) {
                contactRepository.syncWithEmployee(
                    employeeId = employeeId,
                    employeeRequest = employeeRequest
                )
            }

            updateCount
        }
    }

    override fun delete(employeeId: UUID): Int {
        return transactionWithSchema(schema = sessionContext.schema) {
            EmployeeTable.deleteWhere {
                id eq employeeId
            }
        }
    }

    override fun deleteAll(): Int {
        return transactionWithSchema(schema = sessionContext.schema) {
            EmployeeTable.deleteAll()
        }
    }

    override fun count(): Int {
        return transactionWithSchema(schema = sessionContext.schema) {
            EmployeeTable.selectAll().count().toInt()
        }
    }

    /**
     * Populates an SQL [UpdateBuilder] with data from an [EmployeeRequest] instance,
     * so that it can be used to update or create a database record.
     */
    private fun UpdateBuilder<Int>.mapEmployeeRequest(employeeRequest: EmployeeRequest, withTimestamp: Boolean = true) {
        this[EmployeeTable.firstName] = employeeRequest.firstName.trim()
        this[EmployeeTable.lastName] = employeeRequest.lastName.trim()
        this[EmployeeTable.dob] = employeeRequest.dob
        this[EmployeeTable.maritalStatus] = employeeRequest.maritalStatus
        this[EmployeeTable.honorific] = employeeRequest.honorific
        if (withTimestamp) this[EmployeeTable.updatedAt] = DateTimeUtils.currentUTCDateTime()
    }
}
