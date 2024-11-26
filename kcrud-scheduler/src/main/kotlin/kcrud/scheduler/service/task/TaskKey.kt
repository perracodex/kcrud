/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.scheduler.service.task

import kcrud.core.plugins.Uuid
import kcrud.core.util.toUuid
import kotlinx.serialization.Serializable
import org.quartz.JobKey
import org.quartz.Scheduler

/**
 * Represents a key that uniquely identifies a task in the scheduler.
 *
 * @property groupId The group to which the task belongs.
 * @property taskId The unique identifier of the task.
 * @property description The description of the task.
 */
@Serializable
public data class TaskKey private constructor(
    val groupId: Uuid,
    val taskId: String,
    val description: String?
) {
    public companion object {
        /**
         * Creates a [TaskKey] from a Quartz [JobKey].
         *
         * @param jobKey The Quartz [JobKey].
         * @param scheduler The Quartz [Scheduler] instance.
         * @return The [TaskKey] instance.
         */
        internal fun fromJobKey(scheduler: Scheduler, jobKey: JobKey): TaskKey {
            val description: String? = scheduler.getJobDetail(jobKey).description
            return TaskKey(
                groupId = jobKey.group.toUuid(),
                taskId = jobKey.name,
                description = description
            )
        }
    }
}
