/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.domain.employment.repository

import io.perracodex.exposed.pagination.Page
import io.perracodex.exposed.pagination.Pageable
import io.perracodex.exposed.pagination.paginate
import kcrud.core.context.SessionContext
import kcrud.core.database.extensions.exists
import kcrud.core.database.schema.contact.ContactTable
import kcrud.core.database.schema.employee.EmployeeTable
import kcrud.core.database.schema.employment.EmploymentTable
import kcrud.core.database.utils.transaction
import kcrud.domain.employment.model.Employment
import kcrud.domain.employment.model.EmploymentRequest
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import kotlin.uuid.Uuid

/**
 * Implementation of the [IEmploymentRepository] interface.
 * Responsible for managing employment data.
 */
internal class EmploymentRepository(
    private val sessionContext: SessionContext
) : IEmploymentRepository {

    override fun findAll(pageable: Pageable?): Page<Employment> {
        return transaction(sessionContext = sessionContext) {
            EmploymentTable
                .innerJoin(EmployeeTable)
                .leftJoin(ContactTable)
                .selectAll()
                .paginate(pageable = pageable, transform = Employment)
        }
    }

    override fun findById(employeeId: Uuid, employmentId: Uuid): Employment? {
        return transaction(sessionContext = sessionContext) {
            EmploymentTable
                .innerJoin(EmployeeTable)
                .leftJoin(ContactTable)
                .selectAll().where {
                    (EmploymentTable.id eq employmentId) and
                            (EmploymentTable.employeeId eq employeeId)
                }.singleOrNull()?.let { resultRow ->
                    Employment.from(row = resultRow)
                }
        }
    }

    override fun findByEmployeeId(employeeId: Uuid): List<Employment> {
        return transaction(sessionContext = sessionContext) {
            EmploymentTable
                .innerJoin(EmployeeTable)
                .leftJoin(ContactTable)
                .selectAll().where {
                    EmploymentTable.employeeId eq employeeId
                }.map { resultRow ->
                    Employment.from(row = resultRow)
                }
        }
    }

    override fun create(employeeId: Uuid, request: EmploymentRequest): Employment? {
        return transaction(sessionContext = sessionContext) {
            employeeExists(employeeId = employeeId).takeIf { it }?.let {
                EmploymentTable.insert { statement ->
                    statement.toStatement(
                        employeeId = employeeId,
                        request = request
                    )
                }[EmploymentTable.id].let { employmentId ->
                    findById(employeeId = employeeId, employmentId = employmentId)
                        ?: throw IllegalStateException("Failed to create Employment.")
                }
            }
        }
    }

    override fun update(employeeId: Uuid, employmentId: Uuid, request: EmploymentRequest): Employment? {
        return transaction(sessionContext = sessionContext) {
            EmploymentTable.update(
                where = {
                    (EmploymentTable.employeeId eq employeeId) and (EmploymentTable.id eq employmentId)
                }
            ) { statement ->
                statement.toStatement(
                    employeeId = employeeId,
                    request = request
                )
            }.takeIf { it > 0 }?.let {
                findById(employeeId = employeeId, employmentId = employmentId)
            }
        }
    }

    override fun delete(employmentId: Uuid): Int {
        return transaction(sessionContext = sessionContext) {
            EmploymentTable.deleteWhere {
                id eq employmentId
            }
        }
    }

    override fun deleteAll(employeeId: Uuid): Int {
        return transaction(sessionContext = sessionContext) {
            EmploymentTable.deleteWhere {
                EmploymentTable.employeeId eq employeeId
            }
        }
    }

    override fun count(employeeId: Uuid?): Int {
        return transaction(sessionContext = sessionContext) {
            EmploymentTable.selectAll().apply {
                employeeId?.let {
                    where { EmploymentTable.employeeId eq employeeId }
                }
            }.count().toInt()
        }
    }

    override fun employeeExists(employeeId: Uuid): Boolean {
        return transaction(sessionContext = sessionContext) {
            EmployeeTable.selectAll().where { EmployeeTable.id eq employeeId }.exists()
        }
    }

    /**
     * Populates an SQL [UpdateBuilder] with data from an [EmploymentRequest] instance,
     * so that it can be used to update or create a database record.
     */
    private fun UpdateBuilder<Int>.toStatement(employeeId: Uuid, request: EmploymentRequest) {
        this[EmploymentTable.employeeId] = employeeId
        this[EmploymentTable.status] = request.status
        this[EmploymentTable.probationEndDate] = request.probationEndDate
        this[EmploymentTable.workModality] = request.workModality
        this[EmploymentTable.isActive] = request.period.isActive
        this[EmploymentTable.startDate] = request.period.startDate
        this[EmploymentTable.endDate] = request.period.endDate
        this[EmploymentTable.comments] = request.period.comments?.trim()
    }
}
