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
 * @property isDurable Whether the task should remain stored after it is orphaned (no Triggers point to it).
 * @property shouldRecover Whether the task should be re-executed if a 'recovery' or 'fail-over' situation is encountered.
 * @property dataMap Concrete state information for a Task instance.
 */
@Serializable
data class TaskScheduleEntity(
    val name: String,
    val group: String,
    val className: String,
    val nextFireTime: KLocalDateTime?,
    val state: String,
    val isDurable: Boolean,
    val shouldRecover: Boolean,
    val dataMap: String,
)
