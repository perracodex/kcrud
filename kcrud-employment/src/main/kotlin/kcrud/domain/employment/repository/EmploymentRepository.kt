/*
 * Copyright (c) 2024-Present Perracodex. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.domain.employment.repository

import kcrud.base.database.schema.contact.ContactTable
import kcrud.base.database.schema.employee.EmployeeTable
import kcrud.base.database.schema.employment.EmploymentTable
import kcrud.base.database.service.transactionWithSchema
import kcrud.base.env.SessionContext
import kcrud.base.persistence.pagination.Page
import kcrud.base.persistence.pagination.Pageable
import kcrud.base.persistence.pagination.applyPagination
import kcrud.base.utils.DateTimeUtils
import kcrud.domain.employment.entity.EmploymentEntity
import kcrud.domain.employment.entity.EmploymentRequest
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import java.util.*

/**
 * Implementation of the [IEmploymentRepository] interface.
 * Responsible for managing employment data.
 */
internal class EmploymentRepository(
    private val sessionContext: SessionContext
) : IEmploymentRepository {

    override fun findAll(pageable: Pageable?): Page<EmploymentEntity> {
        return transactionWithSchema(schema = sessionContext.schema) {
            val totalElements: Int = EmploymentTable.selectAll().count().toInt()

            val content: List<EmploymentEntity> = (EmploymentTable innerJoin EmployeeTable leftJoin ContactTable)
                .selectAll()
                .applyPagination(pageable = pageable)
                .map { resultRow ->
                    EmploymentEntity.from(row = resultRow)
                }

            Page.build(
                content = content,
                totalElements = totalElements,
                pageable = pageable
            )
        }
    }

    override fun findById(employeeId: UUID, employmentId: UUID): EmploymentEntity? {
        return transactionWithSchema(schema = sessionContext.schema) {
            (EmploymentTable innerJoin EmployeeTable leftJoin ContactTable)
                .selectAll().where {
                    (EmploymentTable.id eq employmentId) and
                            (EmploymentTable.employeeId eq employeeId)
                }.singleOrNull()?.let { resultRow ->
                    EmploymentEntity.from(row = resultRow)
                }
        }
    }

    override fun findByEmployeeId(employeeId: UUID): List<EmploymentEntity> {
        return transactionWithSchema(schema = sessionContext.schema) {
            (EmploymentTable innerJoin EmployeeTable leftJoin ContactTable)
                .selectAll().where {
                    (EmploymentTable.employeeId eq employeeId)
                }
                .map { resultRow ->
                    EmploymentEntity.from(row = resultRow)
                }
        }
    }

    override fun create(employeeId: UUID, employmentRequest: EmploymentRequest): UUID {
        return transactionWithSchema(schema = sessionContext.schema) {
            EmploymentTable.insert { employmentRow ->
                employmentRow.mapEmploymentRequest(
                    employeeId = employeeId,
                    employmentRequest = employmentRequest,
                    withTimestamp = false
                )
            } get EmploymentTable.id
        }
    }

    override fun update(employeeId: UUID, employmentId: UUID, employmentRequest: EmploymentRequest): Int {
        return transactionWithSchema(schema = sessionContext.schema) {
            EmploymentTable.update(where = {
                (EmploymentTable.employeeId eq employeeId) and
                        (EmploymentTable.id eq employmentId)
            }) { employmentRow ->
                employmentRow.mapEmploymentRequest(
                    employeeId = employeeId,
                    employmentRequest = employmentRequest
                )
            }
        }
    }

    override fun delete(employmentId: UUID): Int {
        return transactionWithSchema(schema = sessionContext.schema) {
            EmploymentTable.deleteWhere {
                id eq employmentId
            }
        }
    }

    override fun deleteAll(employeeId: UUID): Int {
        return transactionWithSchema(schema = sessionContext.schema) {
            EmploymentTable.deleteWhere {
                EmploymentTable.employeeId eq employeeId
            }
        }
    }

    override fun count(employeeId: UUID?): Int {
        return transactionWithSchema(schema = sessionContext.schema) {
            employeeId?.let { id ->
                EmploymentTable.select(column = EmploymentTable.employeeId eq id).count().toInt()
            } ?: EmploymentTable.selectAll().count().toInt()
        }
    }

    /**
     * Populates an SQL [UpdateBuilder] with data from an [EmploymentRequest] instance,
     * so that it can be used to update or create a database record.
     */
    private fun UpdateBuilder<Int>.mapEmploymentRequest(
        employeeId: UUID,
        employmentRequest: EmploymentRequest,
        withTimestamp: Boolean = true
    ) {
        this[EmploymentTable.employeeId] = employeeId
        this[EmploymentTable.status] = employmentRequest.status
        this[EmploymentTable.probationEndDate] = employmentRequest.probationEndDate
        this[EmploymentTable.workModality] = employmentRequest.workModality
        this[EmploymentTable.isActive] = employmentRequest.period.isActive
        this[EmploymentTable.startDate] = employmentRequest.period.startDate
        this[EmploymentTable.endDate] = employmentRequest.period.endDate
        this[EmploymentTable.comments] = employmentRequest.period.comments?.trim()
        if (withTimestamp) this[EmploymentTable.updatedAt] = DateTimeUtils.currentUTCDateTime()
    }
}
