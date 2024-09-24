/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.core.env.health.checks

import kcrud.core.env.health.annotation.HealthCheckAPI
import kcrud.core.scheduler.service.SchedulerService
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
public class SchedulerCheck private constructor(
    public val errors: MutableList<String>,
    public val isStarted: Boolean,
    public val isPaused: Boolean,
    public val totalTasks: Int
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
