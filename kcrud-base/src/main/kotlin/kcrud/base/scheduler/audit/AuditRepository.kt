/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.scheduler.audit

import kcrud.base.database.schema.scheduler.SchedulerAuditTable
import kcrud.base.scheduler.audit.entity.AuditEntity
import kcrud.base.scheduler.audit.entity.AuditRequest
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

/**
 * Repository to manage the persistence aND retrieval of the scheduler audit logs.
 */
internal object AuditRepository {

    /**
     * Creates a new audit entry.
     *
     * @param request The [AuditRequest] to create.
     */
    fun create(request: AuditRequest): UUID {
        return transaction {
            val logId: UUID = SchedulerAuditTable.insert {
                it[taskName] = request.taskName
                it[taskGroup] = request.taskGroup
                it[fireTime] = request.fireTime
                it[runTime] = request.runTime
                it[outcome] = request.outcome
                it[log] = request.log
                it[detail] = request.detail
            } get SchedulerAuditTable.id

            logId
        }
    }

    /**
     * Finds all the audit entries.
     *
     * @return The list of [AuditEntity] instances.
     */
    fun findAll(): List<AuditEntity> {
        return transaction {
            SchedulerAuditTable.selectAll()
                .map {
                    AuditEntity.from(row = it)
                }
        }
    }

    /**
     * Finds a audit entry by task name and task group.
     *
     * @param taskName The name of the task.
     * @param taskGroup The group of the task.
     * @return The found [AuditEntity] instance, or `null` if not found.
     */
    fun find(taskName: String, taskGroup: String): AuditEntity? {
        return transaction {
            SchedulerAuditTable.selectAll()
                .where { SchedulerAuditTable.taskName eq taskName }
                .andWhere { SchedulerAuditTable.taskGroup eq taskGroup }
                .map {
                    AuditEntity.from(row = it)
                }
                .singleOrNull()
        }
    }
}
