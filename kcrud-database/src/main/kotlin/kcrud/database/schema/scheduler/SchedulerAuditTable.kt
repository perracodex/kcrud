/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.database.schema.scheduler

import kcrud.database.column.autoGenerate
import kcrud.database.column.kotlinUuid
import kcrud.database.schema.base.TimestampedTable
import kcrud.database.schema.scheduler.type.TaskOutcome
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import kotlin.uuid.Uuid

/**
 * Database table definition for scheduler audit logs.
 */
public object SchedulerAuditTable : TimestampedTable(name = "scheduler_audit") {
    /**
     * The unique identifier of the audit log.
     */
    public val id: Column<Uuid> = kotlinUuid(
        name = "audit_id"
    ).autoGenerate()

    /**
     * The group to which the task belongs.
     */
    public val groupId: Column<Uuid> = kotlinUuid(
        name = "group_id"
    )

    /**
     * The unique ID of the task that was executed.
     */
    public val taskId: Column<String> = varchar(
        name = "task_id",
        length = 200
    )

    /**
     * The description of the task that was executed
     */
    public val description: Column<String> = text(
        name = "description",
    )

    /**
     * A unique snowflake ID to identify the cluster node that executed the task.
     */
    public val snowflakeId: Column<String> = varchar(
        name = "snowflake_id",
        length = 13
    )

    /**
     * The time the task was scheduled to run.
     */
    public val fireTime: Column<LocalDateTime> = datetime(
        name = "fire_time"
    )

    /**
     * The duration the task took to run.
     */
    public val runTime: Column<Long> = long(
        name = "run_time"
    )

    /**
     * The execution result [TaskOutcome].
     */
    public val outcome: Column<TaskOutcome> = enumerationByName(
        name = "outcome",
        length = 64,
        klass = TaskOutcome::class
    )

    /**
     * The audit log information.
     */
    public val log: Column<String?> = text(
        name = "log",
    ).nullable()

    /**
     * The detail that provides more information about the audit log.
     */
    public val detail: Column<String?> = text(
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
