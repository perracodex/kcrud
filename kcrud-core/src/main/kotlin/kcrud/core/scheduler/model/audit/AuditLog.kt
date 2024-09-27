/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.core.scheduler.model.audit

import kcrud.core.database.schema.scheduler.SchedulerAuditTable
import kcrud.core.persistence.model.Meta
import kcrud.core.plugins.Uuid
import kcrud.core.scheduler.service.task.TaskOutcome
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.ResultRow

/**
 * Represents a concrete scheduler audit log.
 *
 * @property id The unique identifier of the audit log.
 * @property taskName The name of the task.
 * @property taskGroup The group of the task.
 * @property fireTime The actual time the trigger fired.
 * @property runTime The amount of time the task ran for, in milliseconds.
 * @property outcome The log [TaskOutcome] status.
 * @property log The audit log information.
 * @property detail The detail that provides more information about the audit log.
 * @property meta The metadata of the record.
 */
@Serializable
public data class AuditLog(
    val id: Uuid,
    val taskName: String,
    val taskGroup: String,
    val fireTime: LocalDateTime,
    val runTime: Long,
    val outcome: TaskOutcome,
    val log: String?,
    val detail: String?,
    val meta: Meta
) {
    internal companion object {
        /**
         * Maps a [ResultRow] to a [AuditLog] instance.
         *
         * @param row The [ResultRow] to map.
         * @return The mapped [AuditLog] instance.
         */
        fun from(row: ResultRow): AuditLog {
            return AuditLog(
                id = row[SchedulerAuditTable.id],
                taskName = row[SchedulerAuditTable.taskName],
                taskGroup = row[SchedulerAuditTable.taskGroup],
                fireTime = row[SchedulerAuditTable.fireTime],
                runTime = row[SchedulerAuditTable.runTime],
                outcome = row[SchedulerAuditTable.outcome],
                log = row[SchedulerAuditTable.log],
                detail = row[SchedulerAuditTable.detail],
                meta = Meta.from(row = row, table = SchedulerAuditTable)
            )
        }
    }
}
