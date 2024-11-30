/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.scheduler.policy

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Sealed class representing the different backoff strategies for retrying a task.
 */
public sealed class BackoffStrategy {
    /**
     * Data class representing a fixed delay for retrying the task.
     *
     * @property delay The fixed delay for retrying the task.
     */
    public data class Fixed(val delay: Duration = 10.seconds) : BackoffStrategy()

    /**
     * Data class representing an exponential backoff strategy for retrying the task.
     *
     * @property initialDelay The initial delay for retrying the task.
     * @property multiplier The multiplier for the exponential backoff.
     */
    public data class Exponential(val initialDelay: Duration = 10.seconds, val multiplier: Double = 2.0) : BackoffStrategy()

    internal companion object {
        const val FIXED_KEY: String = "#BACKOFF_STRATEGY_FIXED_KEY#"
        const val EXPONENTIAL_KEY: String = "#BACKOFF_STRATEGY_EXPONENTIAL_KEY#"
    }
}
