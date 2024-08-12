/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.database.schema.scheduler

import kcrud.base.scheduler.service.task.TaskOutcome
import kcrud.base.utils.KLocalDateTime
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import java.util.*

/**
 * Database table definition for scheduler audit logs.
 */
object SchedulerAuditTable : Table(name = "scheduler_audit") {
    /**
     * The unique identifier of the audit log.
     */
    val id: Column<UUID> = uuid(
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
    val fireTime: Column<KLocalDateTime> = datetime(
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
     * The creation timestamp of the audit record.
     */
    val createdAt: Column<KLocalDateTime> = datetime(
        name = "created_at"
    ).defaultExpression(defaultValue = CurrentDateTime)

    override val primaryKey: Table.PrimaryKey = PrimaryKey(
        firstColumn = id,
        name = "pk_audit_id"
    )
}
