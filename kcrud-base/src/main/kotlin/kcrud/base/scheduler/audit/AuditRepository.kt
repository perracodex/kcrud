/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.scheduler.audit

import kcrud.base.database.schema.scheduler.SchedulerAuditTable
import kcrud.base.scheduler.annotation.SchedulerAPI
import kcrud.base.scheduler.audit.entity.AuditEntity
import kcrud.base.scheduler.audit.entity.AuditRequest
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

/**
 * Repository to manage the persistence aND retrieval of the scheduler audit logs.
 */
@SchedulerAPI
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
     * Finds all the audit entries, ordered bby the most recent first.
     *
     * @return The list of [AuditEntity] instances.
     */
    fun findAll(): List<AuditEntity> {
        return transaction {
            SchedulerAuditTable.selectAll()
                .orderBy(SchedulerAuditTable.createdAt to SortOrder.DESC)
                .map {
                    AuditEntity.from(row = it)
                }
        }
    }

    /**
     * Finds all the audit logs for a concrete task by name and group, ordered by the most recent first.
     *
     * @param taskName The name of the task.
     * @param taskGroup The group of the task.
     * @return The list of [AuditEntity] instances, or an empty list if none found.
     */
    fun find(taskName: String, taskGroup: String): List<AuditEntity> {
        return transaction {
            SchedulerAuditTable.selectAll()
                .where { SchedulerAuditTable.taskName eq taskName }
                .andWhere { SchedulerAuditTable.taskGroup eq taskGroup }
                .orderBy(SchedulerAuditTable.createdAt to SortOrder.DESC)
                .map {
                    AuditEntity.from(row = it)
                }
        }
    }

    /**
     * Finds the most recent audit log for a specific task.
     *
     * @param taskName The name of the task.
     * @param taskGroup The group of the task.
     * @return The most recent [AuditEntity] instance, or `null` if none found.
     */
    fun mostRecent(taskName: String, taskGroup: String): AuditEntity? {
        return transaction {
            SchedulerAuditTable.selectAll()
                .where { SchedulerAuditTable.taskName eq taskName }
                .andWhere { SchedulerAuditTable.taskGroup eq taskGroup }
                .orderBy(SchedulerAuditTable.createdAt to SortOrder.DESC)
                .limit(n = 1)
                .map {
                    AuditEntity.from(row = it)
                }.singleOrNull()
        }
    }

    /**
     * Returns the total count of audit entries for a specific task.
     *
     * @param taskName The name of the task.
     * @param taskGroup The group of the task.
     * @return The total count of audit entries for the task.
     */
    fun count(taskName: String, taskGroup: String): Int {
        return transaction {
            // addLogger(StdOutSqlLogger)

//            explain {
//                SchedulerAuditTable
//                    .selectAll()
//                    .where { SchedulerAuditTable.taskName eq taskName }
//                    .andWhere { SchedulerAuditTable.taskGroup eq taskGroup }
//            }.forEach(::print)


            SchedulerAuditTable
                .selectAll()
                .where { SchedulerAuditTable.taskName eq taskName }
                .andWhere { SchedulerAuditTable.taskGroup eq taskGroup }
                .count()
                .toInt()
        }
    }
}
