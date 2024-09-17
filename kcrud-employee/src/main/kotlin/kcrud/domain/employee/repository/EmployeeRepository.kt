/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.domain.employee.repository

import kcrud.base.database.schema.contact.ContactTable
import kcrud.base.database.schema.employee.EmployeeTable
import kcrud.base.database.service.transactionWithSchema
import kcrud.base.env.SessionContext
import kcrud.base.persistence.pagination.Page
import kcrud.base.persistence.pagination.Pageable
import kcrud.base.persistence.pagination.paginate
import kcrud.domain.contact.repository.IContactRepository
import kcrud.domain.employee.model.Employee
import kcrud.domain.employee.model.EmployeeFilterSet
import kcrud.domain.employee.model.EmployeeRequest
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import kotlin.uuid.Uuid

/**
 * Implementation of the [IEmployeeRepository] interface.
 * Responsible for managing employee data.
 */
internal class EmployeeRepository(
    private val sessionContext: SessionContext,
    private val contactRepository: IContactRepository
) : IEmployeeRepository {

    override fun findById(employeeId: Uuid): Employee? {
        return transactionWithSchema(schema = sessionContext.schema) {
            EmployeeTable.join(
                otherTable = ContactTable,
                joinType = JoinType.LEFT,
                onColumn = EmployeeTable.id,
                otherColumn = ContactTable.employeeId
            ).selectAll().where {
                EmployeeTable.id eq employeeId
            }.singleOrNull()?.let { resultRow ->
                Employee.from(row = resultRow)
            }
        }
    }

    override fun findAll(pageable: Pageable?): Page<Employee> {
        return transactionWithSchema(schema = sessionContext.schema) {
            val query: Query = EmployeeTable.join(
                otherTable = ContactTable,
                joinType = JoinType.LEFT,
                onColumn = EmployeeTable.id,
                otherColumn = ContactTable.employeeId
            ).selectAll()

            // Determine the total records involved in the query before applying pagination.
            val totalElements: Int = query.count().toInt()

            val content: List<Employee> = query
                .paginate(pageable = pageable)
                .map { resultRow ->
                    Employee.from(row = resultRow)
                }

            Page.build(
                content = content,
                totalElements = totalElements,
                pageable = pageable
            )
        }
    }

    override fun search(filterSet: EmployeeFilterSet, pageable: Pageable?): Page<Employee> {
        return transactionWithSchema(schema = sessionContext.schema) {
            // Start with a base query selecting all the records.
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

            // Determine the total records involved in the query before applying pagination.
            val totalElements: Int = query.count().toInt()

            val content: List<Employee> = query
                .paginate(pageable = pageable)
                .map { resultRow ->
                    Employee.from(row = resultRow)
                }

            Page.build(
                content = content,
                totalElements = totalElements,
                pageable = pageable
            )
        }
    }

    override fun create(request: EmployeeRequest): Employee {
        return transactionWithSchema(schema = sessionContext.schema) {
            val employeeId: Uuid = EmployeeTable.insert { employeeRow ->
                employeeRow.mapEmployeeRequest(request = request)
            } get EmployeeTable.id

            request.contact?.let {
                contactRepository.create(
                    employeeId = employeeId,
                    request = request.contact
                )
            }

            findById(employeeId = employeeId)
                ?: throw IllegalStateException("Failed to create Employee.")
        }
    }

    override fun update(employeeId: Uuid, request: EmployeeRequest): Employee? {
        return transactionWithSchema(schema = sessionContext.schema) {
            val updateCount: Int = EmployeeTable.update(
                where = {
                    EmployeeTable.id eq employeeId
                }
            ) { employeeRow ->
                employeeRow.mapEmployeeRequest(request = request)
            }

            if (updateCount > 0) {
                contactRepository.syncWithEmployee(
                    employeeId = employeeId,
                    employeeRequest = request
                )

                findById(employeeId = employeeId)
            } else {
                null
            }
        }
    }

    override fun delete(employeeId: Uuid): Int {
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
    private fun UpdateBuilder<Int>.mapEmployeeRequest(request: EmployeeRequest) {
        this[EmployeeTable.firstName] = request.firstName.trim()
        this[EmployeeTable.lastName] = request.lastName.trim()
        this[EmployeeTable.dob] = request.dob
        this[EmployeeTable.maritalStatus] = request.maritalStatus
        this[EmployeeTable.honorific] = request.honorific
    }
}
