/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.scheduler.service.policy

import kcrud.core.event.SseService
import kcrud.core.util.DateTimeUtils.current
import kcrud.core.util.DateTimeUtils.formatted
import kotlinx.datetime.LocalDateTime
import org.quartz.*
import java.util.*
import kotlin.math.pow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Class responsible for handling task retries with backoff policies.
 */
internal class TaskRetryHandler(
    private val scheduler: Scheduler,
    private val jobDetail: JobDetail,
    private val jobDataMap: JobDataMap,
    private val retryCount: Int,
    private val error: Exception
) {
    /**
     * Handles retry logic upon task failure.
     */
    fun handleRetry() {
        val jobKey: JobKey = jobDetail.key

        // Extract maxRetries from jobDataMap.
        val maxRetries: Int = jobDataMap[RetryPolicy.MAX_RETRIES_KEY] as? Int ?: 0
        // Reconstruct the BackoffStrategy from the jobDataMap.
        val backoffStrategy: BackoffStrategy = reconstructBackoffStrategy(jobDataMap = jobDataMap)
        // Reconstruct the RetryPolicy.
        val retryPolicy = RetryPolicy(maxRetries = maxRetries, backoffStrategy = backoffStrategy)

        if (retryCount >= retryPolicy.maxRetries) {
            // No more retries allowed.
            SseService.push(
                message = "${LocalDateTime.current().formatted(timeDelimiter = " | ", precision = 6)} " +
                        "| Failed and reached max retries " +
                        "| Group Id: ${jobKey.group} " +
                        "| Task Id: ${jobKey.name} " +
                        "| Error: ${error.message.orEmpty()}"
            )
            // Rethrow the exception to allow it to propagate.
            throw error
        } else {
            // Schedule a retry.
            val newRetryCount: Int = retryCount + 1
            val delay: Duration = calculateBackoffDelay(retryPolicy = retryPolicy, retryCount = newRetryCount)
            val retryTimeMs: Long = System.currentTimeMillis() + delay.inWholeMilliseconds

            // Get the next scheduled execution time of the original triggers.
            val triggers: List<Trigger?>? = scheduler.getTriggersOfJob(jobKey)
            val nextFireTimes: List<Long>? = triggers?.mapNotNull { it?.nextFireTime?.time }
            val nextScheduledTimeMs: Long? = nextFireTimes?.minOrNull()

            // Decide whether to schedule a retry.
            if (nextScheduledTimeMs != null && nextScheduledTimeMs <= retryTimeMs) {
                // The next scheduled execution occurs before the retry, skip retry.
                SseService.push(
                    message = "${LocalDateTime.current().formatted(timeDelimiter = " | ", precision = 6)} " +
                            "| Failed but next scheduled execution is earlier than retry " +
                            "| Skipping retry " +
                            "| Group Id: ${jobKey.group} " +
                            "| Task Id: ${jobKey.name} " +
                            "| Error: ${error.message.orEmpty()}"
                )
            } else {
                // Proceed to schedule the retry.
                // Build a new trigger for the existing job.
                val retryTriggerKey = TriggerKey("${jobKey.name}-retry-$newRetryCount", jobKey.group)
                val newJobDataMap = JobDataMap(jobDetail.jobDataMap)
                newJobDataMap[RetryPolicy.COUNT_KEY] = newRetryCount

                val retryTrigger: Trigger = TriggerBuilder.newTrigger()
                    .withIdentity(retryTriggerKey)
                    .startAt(Date(retryTimeMs))
                    .usingJobData(newJobDataMap)
                    .forJob(jobDetail)
                    .build()

                scheduler.scheduleJob(retryTrigger)

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
     * Reconstructs the BackoffStrategy from the JobDataMap.
     */
    private fun reconstructBackoffStrategy(jobDataMap: JobDataMap): BackoffStrategy {
        val backoffStrategyType: String? = jobDataMap[RetryPolicy.BACKOFF_TYPE_KEY] as? String

        return when (backoffStrategyType) {
            BackoffStrategy.FIXED_KEY -> {
                val delayMs: Long = jobDataMap[RetryPolicy.DELAY_MS_KEY] as? Long ?: 10.seconds.inWholeMilliseconds
                BackoffStrategy.Fixed(delay = delayMs.milliseconds)
            }

            BackoffStrategy.EXPONENTIAL_KEY -> {
                val initialDelayMs: Long = jobDataMap[RetryPolicy.INITIAL_DELAY_MS_KEY] as? Long ?: 10.seconds.inWholeMilliseconds
                val multiplier: Double = jobDataMap[RetryPolicy.MULTIPLIER_KEY] as? Double ?: 2.0
                BackoffStrategy.Exponential(initialDelay = initialDelayMs.milliseconds, multiplier = multiplier)
            }

            else -> BackoffStrategy.Fixed()
        }
    }

    /**
     * Calculates the backoff delay based on the retry policy and current retry count.
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
}
