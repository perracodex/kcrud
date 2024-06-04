/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.scheduler.service

import kcrud.base.utils.KLocalDateTime
import kotlin.time.Duration

/**
 * Sealed class representing the different ways a task can be scheduled to start.
 */
sealed class TaskStartAt {

    /**
     * Object representing the current time.
     */
    data object Immediate : TaskStartAt()

    /**
     * Data class representing a specific date and time.
     *
     * @param datetime The date and time at which the task should start.
     */
    data class AtDateTime(val datetime: KLocalDateTime) : TaskStartAt()

    /**
     * Data class representing a duration from the current time.
     *
     * @param duration The duration from the current time at which the task should start.
     */
    data class AfterDuration(val duration: Duration) : TaskStartAt()
}
