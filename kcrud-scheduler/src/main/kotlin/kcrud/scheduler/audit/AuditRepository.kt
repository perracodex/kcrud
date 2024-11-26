/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.scheduler.audit

import io.perracodex.exposed.pagination.Page
import io.perracodex.exposed.pagination.Pageable
import io.perracodex.exposed.pagination.paginate
import kcrud.database.schema.scheduler.SchedulerAuditTable
import kcrud.database.schema.scheduler.type.TaskOutcome
import kcrud.scheduler.model.audit.AuditLog
import kcrud.scheduler.model.audit.AuditLogRequest
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.uuid.Uuid

/**
 * Repository to manage the persistence aND retrieval of the scheduler audit logs.
 */
internal object AuditRepository {

    /**
     * Creates a new audit entry.
     *
     * @param request The [AuditLogRequest] to create.
     */
    fun create(request: AuditLogRequest): Uuid {
        return transaction {
            SchedulerAuditTable.insert { statement ->
                statement[groupId] = request.groupId
                statement[taskId] = request.taskId
                statement[description] = request.description
                statement[snowflakeId] = request.snowflakeId
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
     * @param pageable Optional pagination information.
     * @return The list of [AuditLog] instances.
     */
    fun findAll(pageable: Pageable?): Page<AuditLog> {
        return transaction {
            SchedulerAuditTable.selectAll()
                .orderBy(SchedulerAuditTable.createdAt to SortOrder.DESC)
                .paginate(pageable = pageable, transform = AuditLog)
        }
    }

    /**
     * Finds all the audit logs for a concrete task by name and group, ordered by the most recent first.
     *
     * @param pageable Optional pagination information.
     * @param groupId The group of the task.
     * @param taskId Optional unique identifier of the task. If omitted, all tasks in the group are returned.
     * @return The list of [AuditLog] instances, or an empty list if none found.
     */
    fun find(pageable: Pageable?, groupId: Uuid?, taskId: String?): Page<AuditLog> {
        return transaction {
            SchedulerAuditTable
                .selectAll()
                .apply {
                    groupId?.let {
                        andWhere {
                            SchedulerAuditTable.groupId eq groupId
                        }
                    }
                    taskId?.let {
                        andWhere {
                            SchedulerAuditTable.taskId eq taskId
                        }
                    }
                }
                .orderBy(SchedulerAuditTable.createdAt to SortOrder.DESC)
                .paginate(pageable = pageable, transform = AuditLog)
        }
    }

    /**
     * Finds the most recent audit log for a specific task.
     *
     * @param groupId The group of the task.
     * @param taskId The unique identifier of the task.
     * @return The most recent [AuditLog] instance, or `null` if none found.
     */
    fun mostRecent(groupId: Uuid, taskId: String): AuditLog? {
        return transaction {
            SchedulerAuditTable.selectAll()
                .where { SchedulerAuditTable.groupId eq groupId }
                .andWhere { SchedulerAuditTable.taskId eq taskId }
                .orderBy(SchedulerAuditTable.createdAt to SortOrder.DESC)
                .limit(count = 1)
                .map { AuditLog.from(row = it) }
                .singleOrNull()
        }
    }

    /**
     * Returns the total count of audit entries for a specific task.
     *
     * @param groupId The group of the task.
     * @param taskId The unique identifier of the task.
     * @return The total count of audit entries for the task.
     */
    fun count(groupId: Uuid, taskId: String): Int {
        return transaction {
            // Enabled to print the SQL query for debugging purposes.
            // addLogger(StdOutSqlLogger)

            // Enable to get a detailed explanation of the query.
            // explain {
            //    SchedulerAuditTable
            //        .selectAll()
            //        .where { SchedulerAuditTable.taskId eq taskId }
            //        .andWhere { SchedulerAuditTable.taskGroup eq taskGroup }
            // }.forEach(::print)

            SchedulerAuditTable
                .selectAll()
                .where { SchedulerAuditTable.groupId eq groupId }
                .andWhere { SchedulerAuditTable.taskId eq taskId }
                .count()
                .toInt()
        }
    }

    /**
     * Returns the total count of execution failures for a specific task.
     *
     * @param groupId The group of the task.
     * @param taskId The unique identifier of the task.
     * @return The total count of audit entries for the task.
     */
    fun failures(groupId: Uuid, taskId: String): Int {
        return transaction {
            SchedulerAuditTable
                .selectAll()
                .where { SchedulerAuditTable.groupId eq groupId }
                .andWhere { SchedulerAuditTable.taskId eq taskId }
                .andWhere { SchedulerAuditTable.outcome eq TaskOutcome.ERROR }
                .count()
                .toInt()
        }
    }
}
