/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.scheduler.entity

import kcrud.base.utils.KLocalDateTime
import kotlinx.serialization.Serializable

/**
 * Entity representing the details of a scheduled task.
 *
 * @property name The name of the task.
 * @property group The group of the task.
 * @property className the instance of task that is executed.
 * @property nextFireTime The next time the task is scheduled to be executed. Or null if it is not scheduled.
 * @property state The state of the task, eg: 'PAUSED', 'NORMAL', etc.
 * @property interval The interval at which the task should repeat.
 * @property runs The number of times the task has been triggered.
 * @property dataMap Concrete state information for a Task instance.
 */
@Serializable
data class TaskScheduleEntity(
    val name: String,
    val group: String,
    val className: String,
    val nextFireTime: KLocalDateTime?,
    val state: String,
    val interval: String?,
    val runs: Int,
    val dataMap: String,
)
