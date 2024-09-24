/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.domain.employee.repository

import io.perracodex.exposed.pagination.Page
import io.perracodex.exposed.pagination.Pageable
import io.perracodex.exposed.pagination.paginate
import kcrud.base.database.schema.contact.ContactTable
import kcrud.base.database.schema.employee.EmployeeTable
import kcrud.base.database.utils.transaction
import kcrud.base.env.SessionContext
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
        return transaction(sessionContext = sessionContext) {
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
        return transaction(sessionContext = sessionContext) {
            EmployeeTable.join(
                otherTable = ContactTable,
                joinType = JoinType.LEFT,
                onColumn = EmployeeTable.id,
                otherColumn = ContactTable.employeeId
            ).selectAll().paginate(pageable = pageable, transform = Employee)
        }
    }

    override fun search(filterSet: EmployeeFilterSet, pageable: Pageable?): Page<Employee> {
        return transaction(sessionContext = sessionContext) {
            EmployeeTable.selectAll().apply {
                // Apply filters dynamically based on the presence of criteria in filterSet.
                // Using lowerCase() to make the search case-insensitive.
                // This could be removed if the database is configured to use a case-insensitive collation.

                filterSet.firstName?.let { firstName ->
                    andWhere {
                        EmployeeTable.firstName.lowerCase() like "%${firstName.lowercase()}%"
                    }
                }
                filterSet.lastName?.let { lastName ->
                    andWhere {
                        EmployeeTable.lastName.lowerCase() like "%${lastName.lowercase()}%"
                    }
                }
                filterSet.honorific?.let { honorificList ->
                    if (honorificList.isNotEmpty()) {
                        andWhere {
                            EmployeeTable.honorific inList honorificList
                        }
                    }
                }
                filterSet.maritalStatus?.let { maritalStatusList ->
                    if (maritalStatusList.isNotEmpty()) {
                        andWhere {
                            EmployeeTable.maritalStatus inList maritalStatusList
                        }
                    }
                }
            }.paginate(pageable = pageable, transform = Employee)
        }
    }

    override fun create(request: EmployeeRequest): Employee {
        return transaction(sessionContext = sessionContext) {
            EmployeeTable.insert { statement ->
                statement.toStatement(request = request)
            }[EmployeeTable.id].let { employeeId ->
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
    }

    override fun update(employeeId: Uuid, request: EmployeeRequest): Employee? {
        return transaction(sessionContext = sessionContext) {
            EmployeeTable.update(
                where = {
                    EmployeeTable.id eq employeeId
                }
            ) { statement ->
                statement.toStatement(request = request)
            }.takeIf { it > 0 }?.let {
                contactRepository.syncWithEmployee(
                    employeeId = employeeId,
                    employeeRequest = request
                )

                findById(employeeId = employeeId)
            }
        }
    }

    override fun delete(employeeId: Uuid): Int {
        return transaction(sessionContext = sessionContext) {
            EmployeeTable.deleteWhere {
                id eq employeeId
            }
        }
    }

    override fun deleteAll(): Int {
        return transaction(sessionContext = sessionContext) {
            EmployeeTable.deleteAll()
        }
    }

    override fun count(): Int {
        return transaction(sessionContext = sessionContext) {
            EmployeeTable.selectAll().count().toInt()
        }
    }

    /**
     * Populates an SQL [UpdateBuilder] with data from an [EmployeeRequest] instance,
     * so that it can be used to update or create a database record.
     */
    private fun UpdateBuilder<Int>.toStatement(request: EmployeeRequest) {
        this[EmployeeTable.firstName] = request.firstName.trim()
        this[EmployeeTable.lastName] = request.lastName.trim()
        this[EmployeeTable.dob] = request.dob
        this[EmployeeTable.maritalStatus] = request.maritalStatus
        this[EmployeeTable.honorific] = request.honorific
    }
}
