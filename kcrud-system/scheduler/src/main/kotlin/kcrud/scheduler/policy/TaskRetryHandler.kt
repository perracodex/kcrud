/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.scheduler.policy

import kcrud.core.event.SseService
import kcrud.core.util.DateTimeUtils.current
import kcrud.core.util.DateTimeUtils.formatted
import kotlinx.datetime.LocalDateTime
import org.quartz.*
import java.util.*
import kotlin.math.pow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Handles retry logic for failed tasks based on a defined [RetryPolicy].
 *
 * This class is responsible for determining whether a failed task should be retried
 * and scheduling the retry with an appropriate backoff strategy. It ensures that retries
 * do not interfere with the next scheduled execution of recurring tasks.
 *
 * @property scheduler The Quartz [Scheduler] instance used to schedule retry triggers.
 * @property jobDetail The [JobDetail] associated with the task being retried.
 * @property jobDataMap The [JobDataMap] containing task-specific data, including retry information.
 * @property retryCount The current number of retries that have been attempted for the task.
 * @property error The [Exception] that caused the task to fail, and therefore be retried.
 */
internal class TaskRetryHandler(
    private val scheduler: Scheduler,
    private val jobDetail: JobDetail,
    private val jobDataMap: JobDataMap,
    private val retryCount: Int,
    private val error: Exception
) {
    /**
     * Executes the retry logic based on the [RetryPolicy] and schedules a retry if applicable.
     *
     * - **If the maximum number of retries has been reached**:
     *      - Resets the retry count in the [JobDataMap].
     *      - Logs a failure message indicating that the task has reached its maximum retries.
     *      - Rethrows the exception to signal Quartz of the job failure.
     *
     * - **If retries are still available**:
     *      - Increments the retry count.
     *      - Calculates the backoff delay using the defined strategy.
     *      - Determines whether scheduling a retry would conflict with the next scheduled execution.
     *          - **If a conflict exists**: Resets the retry count and skips scheduling the retry.
     *          - **If no conflict**: Schedules a new retry trigger with the updated retry count and delay.
     *
     * This method ensures that retries do not overlap with future task executions, maintaining the integrity
     * of recurring schedules.
     *
     * @throws Exception Rethrows the original exception if the maximum number of retries has been reached.
     */
    fun handleRetry() {
        val jobKey: JobKey = jobDetail.key

        // Extract maximum allowed retries from JobDataMap.
        val maxRetries: Int = jobDataMap[RetryPolicy.MAX_RETRIES_KEY] as? Int ?: 0

        // Reconstruct the BackoffStrategy from the jobDataMap.
        val backoffStrategy: BackoffStrategy = reconstructBackoffStrategy(jobDataMap = jobDataMap)

        // Reconstruct the RetryPolicy using the extracted parameters.
        val retryPolicy = RetryPolicy(
            maxRetries = maxRetries,
            backoffStrategy = backoffStrategy
        )

        if (retryCount >= retryPolicy.maxRetries) {
            // Reset retry count as max retries have been reached.
            jobDataMap[RetryPolicy.COUNT_KEY] = 0

            // Log the failure and indicate that maximum retries have been reached.
            SseService.push(
                message = "${LocalDateTime.current().formatted(timeDelimiter = " | ", precision = 6)} " +
                        "| Failed and reached max retries " +
                        "| Group Id: ${jobKey.group} " +
                        "| Task Id: ${jobKey.name} " +
                        "| Error: ${error.message.orEmpty()}"
            )

            // Rethrow the exception to signal Quartz of the job failure.
            throw error
        } else {
            // Schedule a retry attempt.
            val newRetryCount: Int = retryCount + 1
            val delay: Duration = calculateBackoffDelay(retryPolicy = retryPolicy, retryCount = newRetryCount)
            val retryTimeMs: Long = System.currentTimeMillis() + delay.inWholeMilliseconds

            // Retrieve all triggers associated with the job to determine the next scheduled execution time.
            val triggers: List<Trigger?>? = scheduler.getTriggersOfJob(jobKey)
            val nextFireTimes: List<Long>? = triggers?.mapNotNull { it?.nextFireTime?.time }
            val nextScheduledTimeMs: Long? = nextFireTimes?.minOrNull()

            // Determine whether scheduling the retry would conflict with the next execution time.
            if (nextScheduledTimeMs != null && nextScheduledTimeMs <= retryTimeMs) {
                // Reset retry count as retry would interfere with the next scheduled execution.
                jobDataMap[RetryPolicy.COUNT_KEY] = 0

                // Log that the retry is being skipped due to scheduling conflict.
                SseService.push(
                    message = "${LocalDateTime.current().formatted(timeDelimiter = " | ", precision = 6)} " +
                            "| Failed but next scheduled execution is earlier than retry " +
                            "| Skipping retry " +
                            "| Group Id: ${jobKey.group} " +
                            "| Task Id: ${jobKey.name} " +
                            "| Error: ${error.message.orEmpty()}"
                )
            } else {
                // Proceed to schedule the retry with the updated retry count and delay.

                // Define a unique key for the retry trigger to avoid conflicts.
                val retryTriggerKey = TriggerKey("${jobKey.name}-retry-$newRetryCount", jobKey.group)

                // Create a new JobDataMap for the retry trigger,
                // preserving existing data and updating the retry count.
                val newJobDataMap = JobDataMap(jobDetail.jobDataMap)
                newJobDataMap[RetryPolicy.COUNT_KEY] = newRetryCount

                // Build the retry trigger with the calculated start time and updated JobDataMap.
                val retryTrigger: Trigger = TriggerBuilder.newTrigger()
                    .withIdentity(retryTriggerKey)
                    .startAt(Date(retryTimeMs))
                    .usingJobData(newJobDataMap)
                    .forJob(jobDetail)
                    .build()

                // Schedule the retry trigger with the scheduler.
                scheduler.scheduleJob(retryTrigger)

                // Log the scheduling of the retry attempt.
                SseService.push(
                    message = "${LocalDateTime.current().formatted(timeDelimiter = " | ", precision = 6)} " +
                            "| Failed and will be retried (#$newRetryCount) after $delay " +
                            "| Group Id: ${jobKey.group} " +
                            "| Task Id: ${jobKey.name} " +
                            "| Error: ${error.message.orEmpty()}"
                )
            }
        }
    }

    /**
     * Reconstructs the [BackoffStrategy] based on the data stored in the [JobDataMap].
     *
     * This method reads the backoff strategy type and its associated parameters from the [JobDataMap]
     * and creates an instance of the corresponding [BackoffStrategy]. If the strategy type is unrecognized
     * or missing, it defaults to a fixed strategy with a 10-second delay.
     *
     * @param jobDataMap The [JobDataMap] containing backoff strategy information.
     * @return An instance of [BackoffStrategy] based on the stored configuration.
     */
    private fun reconstructBackoffStrategy(jobDataMap: JobDataMap): BackoffStrategy {
        val backoffStrategyType: String? = jobDataMap[RetryPolicy.BACKOFF_TYPE_KEY] as? String

        return when (backoffStrategyType) {
            BackoffStrategy.FIXED_KEY -> {
                val delayMs: Long = jobDataMap[RetryPolicy.DELAY_MS_KEY] as? Long ?: DEFAULT_DELAY_MS
                BackoffStrategy.Fixed(delay = delayMs.milliseconds)
            }

            BackoffStrategy.EXPONENTIAL_KEY -> {
                val initialDelayMs: Long = jobDataMap[RetryPolicy.INITIAL_DELAY_MS_KEY] as? Long ?: DEFAULT_DELAY_MS
                val multiplier: Double = jobDataMap[RetryPolicy.MULTIPLIER_KEY] as? Double ?: DEFAULT_MULTIPLIER
                BackoffStrategy.Exponential(initialDelay = initialDelayMs.milliseconds, multiplier = multiplier)
            }

            else -> BackoffStrategy.Fixed()
        }
    }

    /**
     * Calculates the backoff delay based on the provided [RetryPolicy] and the current retry attempt.
     *
     * - **Fixed Strategy**: Returns the predefined fixed delay.
     * - **Exponential Strategy**: Calculates the delay by multiplying the initial delay by the multiplier
     *   raised to the power of (retryCount - 1).
     *
     * @param retryPolicy The [RetryPolicy] defining the backoff strategy.
     * @param retryCount The current retry attempt number.
     * @return The calculated [Duration] to wait before the next retry.
     */
    private fun calculateBackoffDelay(retryPolicy: RetryPolicy, retryCount: Int): Duration {
        return when (val strategy: BackoffStrategy = retryPolicy.backoffStrategy) {
            is BackoffStrategy.Fixed -> strategy.delay

            is BackoffStrategy.Exponential -> {
                val initialDelay: Duration = strategy.initialDelay
                val multiplier: Double = strategy.multiplier
                initialDelay * multiplier.pow(n = retryCount - 1)
            }
        }
    }

    private companion object {
        /** Default delay in milliseconds for backoff strategies when not specified. */
        private const val DEFAULT_DELAY_MS: Long = 10_000L // 10 seconds.

        /** Default multiplier for exponential backoff strategies when not specified. */
        private const val DEFAULT_MULTIPLIER: Double = 2.0
    }
}
