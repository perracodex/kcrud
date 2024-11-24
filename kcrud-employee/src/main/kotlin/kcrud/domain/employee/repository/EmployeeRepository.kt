/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.domain.employee.repository

import io.perracodex.exposed.pagination.Page
import io.perracodex.exposed.pagination.Pageable
import io.perracodex.exposed.pagination.paginate
import kcrud.core.context.SessionContext
import kcrud.core.database.schema.contact.ContactTable
import kcrud.core.database.schema.employee.EmployeeTable
import kcrud.core.database.util.transaction
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

    override fun findByWorkEmail(workEmail: String, excludeEmployeeId: Uuid?): Employee? {
        return transaction(sessionContext = sessionContext) {
            val query: Query = EmployeeTable.join(
                otherTable = ContactTable,
                joinType = JoinType.LEFT,
                onColumn = EmployeeTable.id,
                otherColumn = ContactTable.employeeId
            ).selectAll().where {
                EmployeeTable.workEmail eq workEmail
            }

            excludeEmployeeId?.let {
                query.andWhere {
                    EmployeeTable.id neq excludeEmployeeId
                }
            }

            query.singleOrNull()?.let { resultRow ->
                Employee.from(row = resultRow)
            }
        }
    }

    override fun filter(filterSet: EmployeeFilterSet, pageable: Pageable?): Page<Employee> {
        return transaction(sessionContext = sessionContext) {
            EmployeeTable.join(
                otherTable = ContactTable,
                joinType = JoinType.LEFT,
                onColumn = EmployeeTable.id,
                otherColumn = ContactTable.employeeId
            ).selectAll().apply {
                // Apply filters dynamically based on the presence of criteria in filterSet.
                // Using lowerCase() to make the search case-insensitive.
                // This could be removed if the database is configured to use a case-insensitive collation.

                if (!filterSet.firstName.isNullOrBlank()) {
                    andWhere {
                        EmployeeTable.firstName.lowerCase() like "%${filterSet.firstName.trim().lowercase()}%"
                    }
                }
                if (!filterSet.lastName.isNullOrBlank()) {
                    andWhere {
                        EmployeeTable.lastName.lowerCase() like "%${filterSet.lastName.trim().lowercase()}%"
                    }
                }
                if (!filterSet.workEmail.isNullOrBlank()) {
                    andWhere {
                        EmployeeTable.workEmail.lowerCase() like "%${filterSet.workEmail.trim().lowercase()}%"
                    }
                }
                if (!filterSet.contactEmail.isNullOrBlank()) {
                    andWhere {
                        ContactTable.email.lowerCase() like "%${filterSet.contactEmail.trim().lowercase()}%"
                    }
                }
                if (!filterSet.honorific.isNullOrEmpty()) {
                    andWhere {
                        EmployeeTable.honorific inList filterSet.honorific
                    }
                }
                if (!filterSet.maritalStatus.isNullOrEmpty()) {
                    andWhere {
                        EmployeeTable.maritalStatus inList filterSet.maritalStatus
                    }
                }
            }.paginate(pageable = pageable, transform = Employee)
        }
    }

    override fun search(term: String, pageable: Pageable?): Page<Employee> {
        // Normalize the search term.
        // The lowercasing could be removed if the database is configured to use a case-insensitive collation.
        val searchTerm: String = term.trim().lowercase()

        // Pattern to match any part within an email local segment, before '@'.
        val emailLocalSegmentPattern: Expression<String> = stringParam(value = "([^@]*?$searchTerm[^@]*)")

        return transaction(sessionContext = sessionContext) {
            EmployeeTable.join(
                otherTable = ContactTable,
                joinType = JoinType.LEFT,
                onColumn = EmployeeTable.id,
                otherColumn = ContactTable.employeeId
            ).selectAll().where {
                // Search in first name.
                (EmployeeTable.firstName.lowerCase() like "%$searchTerm%")
            }.orWhere {
                // Search in last name.
                (EmployeeTable.lastName.lowerCase() like "%$searchTerm%")
            }.orWhere {
                // Search within the local part of work email (before '@').
                EmployeeTable.workEmail.regexp(pattern = emailLocalSegmentPattern, caseSensitive = false)
            }.orWhere {
                // Search work email starting with the search term.
                (EmployeeTable.workEmail.lowerCase() like "$searchTerm%")
            }.orWhere {
                // Search work email ending with the search term.
                (EmployeeTable.workEmail.lowerCase() like "%$searchTerm")
            }.orWhere {
                // Search within the local part of contact email (before '@').
                ContactTable.email.regexp(pattern = emailLocalSegmentPattern, caseSensitive = false)
            }.orWhere {
                // Search contact email starting with the search term.
                (ContactTable.email.lowerCase() like "$searchTerm%")
            }.orWhere {
                // Search contact email ending with the search term.
                (ContactTable.email.lowerCase() like "%$searchTerm")
            }.paginate(
                pageable = pageable,
                transform = Employee
            )
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

                val employee: Employee? = findById(employeeId = employeeId)
                checkNotNull(employee) { "Failed to create Employee." }
                employee
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
        this[EmployeeTable.workEmail] = request.workEmail.trim()
        this[EmployeeTable.dob] = request.dob
        this[EmployeeTable.maritalStatus] = request.maritalStatus
        this[EmployeeTable.honorific] = request.honorific
    }
}
