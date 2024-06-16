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
    val id: Column<UUID> = uuid(
        name = "audit_id"
    ).autoGenerate()

    val taskName: Column<String> = varchar(
        name = "task_name",
        length = 200
    )

    val taskGroup: Column<String> = varchar(
        name = "task_group",
        length = 200
    )

    val fireTime: Column<KLocalDateTime> = datetime(
        name = "fire_time"
    )

    val runTime: Column<Long> = long(
        name = "run_time"
    )

    val outcome: Column<TaskOutcome> = enumerationByName(
        name = "outcome",
        length = 64,
        klass = TaskOutcome::class
    )

    val log: Column<String?> = text(
        name = "log",
    ).nullable()

    val detail: Column<String?> = text(
        name = "detail",
    ).nullable()

    val createdAt: Column<KLocalDateTime> = datetime(
        name = "created_at"
    ).defaultExpression(defaultValue = CurrentDateTime)

    override val primaryKey: Table.PrimaryKey = PrimaryKey(
        firstColumn = id,
        name = "pk_audit_id"
    )
}
