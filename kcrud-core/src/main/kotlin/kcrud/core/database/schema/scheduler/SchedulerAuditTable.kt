/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.core.database.schema

import kcrud.core.database.column.autoGenerate
import kcrud.core.database.column.kotlinUuid
import kcrud.core.database.schema.base.TimestampedTable
import kcrud.core.scheduler.service.task.TaskOutcome
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import kotlin.uuid.Uuid

/**
 * Database table definition for scheduler audit logs.
 */
internal object SchedulerAuditTable : TimestampedTable(name = "scheduler_audit") {
    /**
     * The unique identifier of the audit log.
     */
    val id: Column<Uuid> = kotlinUuid(
        name = "audit_id"
    ).autoGenerate()

    /**
     * The group to which the task belongs.
     */
    val groupId: Column<String> = varchar(
        name = "group_id",
        length = 200
    )

    /**
     * The unique ID of the task that was executed.
     */
    val taskId: Column<String> = varchar(
        name = "task_id",
        length = 200
    )

    /**
     * The time the task was scheduled to run.
     */
    val fireTime: Column<LocalDateTime> = datetime(
        name = "fire_time"
    )

    /**
     * The duration the task took to run.
     */
    val runTime: Column<Long> = long(
        name = "run_time"
    )

    /**
     * The execution result [TaskOutcome].
     */
    val outcome: Column<TaskOutcome> = enumerationByName(
        name = "outcome",
        length = 64,
        klass = TaskOutcome::class
    )

    /**
     * The audit log information.
     */
    val log: Column<String?> = text(
        name = "log",
    ).nullable()

    /**
     * The detail that provides more information about the audit log.
     */
    val detail: Column<String?> = text(
        name = "detail",
    ).nullable()

    /**
     * The primary key of the table.
     */
    override val primaryKey: PrimaryKey = PrimaryKey(
        firstColumn = id,
        name = "pk_audit_id"
    )
}
