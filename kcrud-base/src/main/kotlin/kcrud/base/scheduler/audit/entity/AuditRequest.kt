/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.scheduler.audit.entity

import kcrud.base.scheduler.service.task.TaskOutcome
import kcrud.base.utils.KLocalDateTime

/**
 * Represents a log request to be stored in the database.
 *
 * @property taskName The name of the task.
 * @property taskGroup The group of the task.
 * @property fireTime The actual time the trigger fired.
 * @property runTime The amount of time the job ran for, in milliseconds.
 * @property outcome The log outcome status.
 * @property log The log information.
 * @property detail The detail that provides more information about the log.
 */
data class AuditRequest(
    val taskName: String,
    val taskGroup: String,
    val fireTime: KLocalDateTime,
    val runTime: Long,
    val outcome: TaskOutcome,
    val log: String?,
    val detail: String?,
)
