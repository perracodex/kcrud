/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.core.database.schema.scheduler

import kcrud.core.database.columns.autoGenerate
import kcrud.core.database.columns.kotlinUuid
import kcrud.core.database.schema.base.TimestampedTable
import kcrud.core.scheduler.service.task.TaskOutcome
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
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
     * The name of the task that was executed.
     */
    val taskName: Column<String> = varchar(
        name = "task_name",
        length = 200
    )

    /**
     * The group to which the task belongs.
     */
    val taskGroup: Column<String> = varchar(
        name = "task_group",
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

    override val primaryKey: Table.PrimaryKey = PrimaryKey(
        firstColumn = id,
        name = "pk_audit_id"
    )
}
