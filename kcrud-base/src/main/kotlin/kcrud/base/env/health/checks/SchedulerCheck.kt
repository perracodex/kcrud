/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.env.health.checks

import kcrud.base.env.health.annotation.HealthCheckAPI
import kcrud.base.scheduler.service.core.SchedulerService
import kotlinx.coroutines.runBlocking
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
data class SchedulerCheck(
    val errors: MutableList<String>,
    val isStarted: Boolean,
    val isPaused: Boolean,
    val totalTasks: Int,
) {
    constructor() : this(
        errors = mutableListOf(),
        isStarted = SchedulerService.isStarted(),
        isPaused = SchedulerService.isPaused(),
        totalTasks = runBlocking { SchedulerService.totalTasks() },
    ) {
        if (!isStarted) {
            errors.add("${this::class.simpleName}. Scheduler is not started.")
        }
    }
}
