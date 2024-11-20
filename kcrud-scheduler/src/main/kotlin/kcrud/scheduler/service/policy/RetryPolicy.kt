/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.scheduler.service.policy

/**
 * Data class representing the retry policy for a task.
 *
 * @property maxRetries The maximum number of retries allowed for the task.
 * @property backoffStrategy The backoff strategy to use for retrying the task.
 */
public data class RetryPolicy(
    val maxRetries: Int = 3,
    val backoffStrategy: BackoffStrategy = BackoffStrategy.Exponential()
) {
    internal companion object {
        const val COUNT_KEY: String = "#RETRY_COUNT_KEY#"
        const val MAX_RETRIES_KEY: String = "#RETRY_MAX_RETRIES_KEY#"
        const val BACKOFF_TYPE_KEY: String = "#RETRY_BACKOFF_TYPE_KEY#"
        const val DELAY_MS_KEY: String = "#RETRY_DELAY_MS_KEYY#"
        const val INITIAL_DELAY_MS_KEY: String = "#RETRY_INITIAL_DELAY_MS_KEY#"
        const val MULTIPLIER_KEY: String = "#RETRY_MULTIPLIER_KEY#"
    }
}
