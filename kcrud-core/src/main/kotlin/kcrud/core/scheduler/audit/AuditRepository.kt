/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.core.scheduler.audit

import kcrud.core.database.schema.scheduler.SchedulerAuditTable
import kcrud.core.scheduler.model.audit.AuditLog
import kcrud.core.scheduler.model.audit.AuditLogRequest
import kcrud.core.scheduler.service.annotation.SchedulerAPI
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.uuid.Uuid

/**
 * Repository to manage the persistence aND retrieval of the scheduler audit logs.
 */
@SchedulerAPI
internal object AuditRepository {

    /**
     * Creates a new audit entry.
     *
     * @param request The [AuditLogRequest] to create.
     */
    fun create(request: AuditLogRequest): Uuid {
        return transaction {
            SchedulerAuditTable.insert { statement ->
                statement[taskName] = request.taskName
                statement[taskGroup] = request.taskGroup
                statement[fireTime] = request.fireTime
                statement[runTime] = request.runTime
                statement[outcome] = request.outcome
                statement[log] = request.log
                statement[detail] = request.detail
            }[SchedulerAuditTable.id]
        }
    }

    /**
     * Finds all the audit entries, ordered bby the most recent first.
     *
     * @return The list of [AuditLog] instances.
     */
    fun findAll(): List<AuditLog> {
        return transaction {
            SchedulerAuditTable.selectAll()
                .orderBy(SchedulerAuditTable.createdAt to SortOrder.DESC)
                .map { AuditLog.from(row = it) }
        }
    }

    /**
     * Finds all the audit logs for a concrete task by name and group, ordered by the most recent first.
     *
     * @param taskName The name of the task.
     * @param taskGroup The group of the task.
     * @return The list of [AuditLog] instances, or an empty list if none found.
     */
    fun find(taskName: String, taskGroup: String): List<AuditLog> {
        return transaction {
            SchedulerAuditTable.selectAll()
                .where { SchedulerAuditTable.taskName eq taskName }
                .andWhere { SchedulerAuditTable.taskGroup eq taskGroup }
                .orderBy(SchedulerAuditTable.createdAt to SortOrder.DESC)
                .map { AuditLog.from(row = it) }
        }
    }

    /**
     * Finds the most recent audit log for a specific task.
     *
     * @param taskName The name of the task.
     * @param taskGroup The group of the task.
     * @return The most recent [AuditLog] instance, or `null` if none found.
     */
    fun mostRecent(taskName: String, taskGroup: String): AuditLog? {
        return transaction {
            SchedulerAuditTable.selectAll()
                .where { SchedulerAuditTable.taskName eq taskName }
                .andWhere { SchedulerAuditTable.taskGroup eq taskGroup }
                .orderBy(SchedulerAuditTable.createdAt to SortOrder.DESC)
                .limit(n = 1)
                .map { AuditLog.from(row = it) }
                .singleOrNull()
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
            // Enabled to print the SQL query for debugging purposes.
            // addLogger(StdOutSqlLogger)

            // Enable to get a detailed explanation of the query.
            // explain {
            //    SchedulerAuditTable
            //        .selectAll()
            //        .where { SchedulerAuditTable.taskName eq taskName }
            //        .andWhere { SchedulerAuditTable.taskGroup eq taskGroup }
            //}.forEach(::print)

            SchedulerAuditTable
                .selectAll()
                .where { SchedulerAuditTable.taskName eq taskName }
                .andWhere { SchedulerAuditTable.taskGroup eq taskGroup }
                .count()
                .toInt()
        }
    }
}
