/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.domain.employment.repository

import kcrud.base.database.schema.contact.ContactTable
import kcrud.base.database.schema.employee.EmployeeTable
import kcrud.base.database.schema.employment.EmploymentTable
import kcrud.base.database.service.transactionWithSchema
import kcrud.base.env.SessionContext
import kcrud.base.persistence.pagination.Page
import kcrud.base.persistence.pagination.Pageable
import kcrud.base.persistence.pagination.paginate
import kcrud.domain.employment.entity.EmploymentEntity
import kcrud.domain.employment.entity.EmploymentRequest
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

    override fun findAll(pageable: Pageable?): Page<EmploymentEntity> {
        return transactionWithSchema(schema = sessionContext.schema) {
            // Need counting the overall elements before applying pagination.
            // A separate simple count query is by far more performant
            // than having a 'count over' expression as part of the main query.
            val totalElements: Int = EmploymentTable.selectAll().count().toInt()

            val content: List<EmploymentEntity> = (EmploymentTable innerJoin EmployeeTable leftJoin ContactTable)
                .selectAll()
                .paginate(pageable = pageable)
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

    override fun findById(employeeId: Uuid, employmentId: Uuid): EmploymentEntity? {
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

    override fun findByEmployeeId(employeeId: Uuid): List<EmploymentEntity> {
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

    override fun create(employeeId: Uuid, employmentRequest: EmploymentRequest): Uuid {
        return transactionWithSchema(schema = sessionContext.schema) {
            EmploymentTable.insert { employmentRow ->
                employmentRow.mapEmploymentRequest(
                    employeeId = employeeId,
                    employmentRequest = employmentRequest
                )
            } get EmploymentTable.id
        }
    }

    override fun update(employeeId: Uuid, employmentId: Uuid, employmentRequest: EmploymentRequest): Int {
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

    override fun delete(employmentId: Uuid): Int {
        return transactionWithSchema(schema = sessionContext.schema) {
            EmploymentTable.deleteWhere {
                id eq employmentId
            }
        }
    }

    override fun deleteAll(employeeId: Uuid): Int {
        return transactionWithSchema(schema = sessionContext.schema) {
            EmploymentTable.deleteWhere {
                EmploymentTable.employeeId eq employeeId
            }
        }
    }

    override fun count(employeeId: Uuid?): Int {
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
        employeeId: Uuid,
        employmentRequest: EmploymentRequest
    ) {
        this[EmploymentTable.employeeId] = employeeId
        this[EmploymentTable.status] = employmentRequest.status
        this[EmploymentTable.probationEndDate] = employmentRequest.probationEndDate
        this[EmploymentTable.workModality] = employmentRequest.workModality
        this[EmploymentTable.isActive] = employmentRequest.period.isActive
        this[EmploymentTable.startDate] = employmentRequest.period.startDate
        this[EmploymentTable.endDate] = employmentRequest.period.endDate
        this[EmploymentTable.comments] = employmentRequest.period.comments?.trim()
    }
}
