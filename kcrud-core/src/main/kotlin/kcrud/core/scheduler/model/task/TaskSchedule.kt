/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.core.scheduler.model.task

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

/**
 * Represents the details of a scheduled task.
 *
 * @property groupId The group of the task.
 * @property taskId The Id of the task.
 * @property snowflakeData The snowflake details of the task.
 * @property consumer The consumer that will execute the task.
 * @property nextFireTime The next time the task is scheduled to be executed. Or null if it is not scheduled.
 * @property state The state of the task, eg: 'PAUSED', 'NORMAL', etc.
 * @property outcome The execution outcome of the task.
 * @property log The log information of the task.
 * @property schedule The schedule at which the task should repeat.
 * @property scheduleInfo Optional information about the schedule. For example a raw cron expression.
 * @property runs The number of times the task has been triggered.
 * @property dataMap Concrete parameters of the task.
 */
@Serializable
public data class TaskSchedule(
    val groupId: String,
    val taskId: String,
    val snowflakeData: String,
    val consumer: String,
    val nextFireTime: LocalDateTime?,
    val state: String,
    val outcome: String?,
    val log: String?,
    val schedule: String?,
    val scheduleInfo: String?,
    val runs: Int?,
    val dataMap: List<String>,
)
