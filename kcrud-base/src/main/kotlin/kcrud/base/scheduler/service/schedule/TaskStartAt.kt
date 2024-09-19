/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.scheduler.service.schedule

import kcrud.base.utils.KLocalDateTime
import kotlin.time.Duration

/**
 * Sealed class representing the different ways a task can be scheduled to start.
 */
public sealed class TaskStartAt {

    /**
     * Object representing the current time.
     */
    public data object Immediate : TaskStartAt()

    /**
     * Data class representing a specific date and time.
     *
     * @property datetime The date and time at which the task should start.
     */
    public data class AtDateTime(val datetime: KLocalDateTime) : TaskStartAt()

    /**
     * Data class representing a duration from the current time.
     *
     * @property duration The duration from the current time at which the task should start.
     */
    public data class AfterDuration(val duration: Duration) : TaskStartAt()
}
