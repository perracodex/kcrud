/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.scheduling.entity

import kotlinx.serialization.Serializable

/**
 * Entity representing the details of a scheduled job.
 *
 * @param name The name of the job.
 * @param group The group of the job.
 * @param className the instance of Job that is executed.
 * @param description The description of the Job.
 * @param isDurable Whether the Job should remain stored after it is orphaned (no Triggers point to it).
 * @param shouldRecover Whether the job should be re-executed if a 'recovery' or 'fail-over' situation is encountered.
 * @param dataMap Concrete state information for Job instance.
 */
@Serializable
data class JobScheduleEntity(
    val name: String,
    val group: String,
    val className: String,
    val description: String?,
    val isDurable: Boolean,
    val shouldRecover: Boolean,
    val dataMap: String,
)
