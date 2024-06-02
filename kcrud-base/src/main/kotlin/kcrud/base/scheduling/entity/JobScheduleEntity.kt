/*
 * Copyright (c) 2024-Present Perracodex. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.scheduling.entity

import kcrud.base.utils.KLocalDateTime
import kotlinx.serialization.Serializable

/**
 * Entity representing the details of a scheduled job.
 *
 * @property name The name of the job.
 * @property group The group of the job.
 * @property className the instance of Job that is executed.
 * @property nextFireTime The next time the Job is scheduled to be executed. Or null if it is not scheduled.
 * @property state The state of the Job, eg: 'PAUSED', 'NORMAL', etc.
 * @property isDurable Whether the Job should remain stored after it is orphaned (no Triggers point to it).
 * @property shouldRecover Whether the job should be re-executed if a 'recovery' or 'fail-over' situation is encountered.
 * @property dataMap Concrete state information for Job instance.
 */
@Serializable
data class JobScheduleEntity(
    val name: String,
    val group: String,
    val className: String,
    val nextFireTime: KLocalDateTime?,
    val state: String,
    val isDurable: Boolean,
    val shouldRecover: Boolean,
    val dataMap: String,
)
