/*
 * Copyright (c) 2024-Present Perracodex. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.scheduling.service

import kcrud.base.utils.KLocalDateTime
import kotlin.time.Duration

/**
 * Sealed class representing the different ways a job can be scheduled to start.
 */
sealed class JobStartAt {

    /**
     * Object representing the current time.
     */
    data object Immediate : JobStartAt()

    /**
     * Data class representing a specific date and time.
     *
     * @param datetime The date and time at which the job should start.
     */
    data class AtDateTime(val datetime: KLocalDateTime) : JobStartAt()

    /**
     * Data class representing a duration from the current time.
     *
     * @param duration The duration from the current time at which the job should start.
     */
    data class AfterDuration(val duration: Duration) : JobStartAt()
}
