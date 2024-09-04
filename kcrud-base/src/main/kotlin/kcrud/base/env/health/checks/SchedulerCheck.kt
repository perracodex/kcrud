/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.env.health.checks

import kcrud.base.env.health.annotation.HealthCheckAPI
import kcrud.base.scheduler.service.core.SchedulerService
import kotlinx.serialization.Serializable

/**
 * Used to check the health of the scheduler.
 *
 * @property errors The list of errors that occurred during the check.
 * @property isStarted Whether the scheduler is started.
 * @property isPaused Whether the scheduler is paused.
 * @property totalTasks The total number of tasks in the scheduler.
 */
@HealthCheckAPI
@Serializable
public data class SchedulerCheck internal constructor(
    val errors: MutableList<String>,
    val isStarted: Boolean,
    val isPaused: Boolean,
    val totalTasks: Int
) {
    init {
        if (!isStarted) {
            errors.add("SchedulerCheck. Scheduler is not started.")
        }
    }

    internal companion object {
        /**
         * Creates a new [SchedulerCheck] instance.
         * We need to use a suspendable factory method as totalTasks is a suspend function.
         */
        suspend fun create(): SchedulerCheck {
            return SchedulerCheck(
                errors = mutableListOf(),
                isStarted = SchedulerService.isStarted(),
                isPaused = SchedulerService.isPaused(),
                totalTasks = SchedulerService.totalTasks()
            )
        }
    }
}
