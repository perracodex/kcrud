/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.scheduler.service.task

import kcrud.core.util.DateTimeUtils.toJavaDate
import kcrud.core.util.DateTimeUtils.toJavaInstant
import kcrud.scheduler.service.SchedulerService
import kcrud.scheduler.service.policy.BackoffStrategy
import kcrud.scheduler.service.policy.RetryPolicy
import kcrud.scheduler.service.schedule.Schedule
import kcrud.scheduler.service.schedule.TaskStartAt
import org.quartz.*
import java.util.*
import kotlin.uuid.Uuid

/**
 * Class to create and send a scheduling request for a task.
 * It supports both simple intervals and cron-based scheduling.
 *
 * @property groupId The Group ID of the task to be scheduled.
 * @property taskId The unique ID of the task to be scheduled.
 * @property consumerClass The class of the task consumer to be scheduled.
 * @property startAt Specifies when the task should start. Defaults to immediate execution.
 * @property parameters Optional parameters to be passed to the task class.
 * @property retryPolicy The retry policy for the task.
 */
public class TaskDispatch(
    private val groupId: Uuid,
    private val taskId: String,
    private val consumerClass: Class<out TaskConsumer<*>>,
    private var startAt: TaskStartAt = TaskStartAt.Immediate,
    private var parameters: Map<String, Any?> = emptyMap(),
    private val retryPolicy: RetryPolicy? = null
) {
    /**
     * Schedule the task to be executed immediately or at a specified [startAt] time.
     */
    public fun send(): TaskKey {
        val job: BasicJob = buildJob()

        // Define the schedule builder and set misfire instructions to
        // handle cases where the trigger misses its scheduled time,
        // in which case the task will be executed immediately.
        val scheduleBuilder: SimpleScheduleBuilder = SimpleScheduleBuilder.simpleSchedule()
            .withMisfireHandlingInstructionFireNow()

        // Send the task to the scheduler.
        val trigger: SimpleTrigger = job.triggerBuilder.withSchedule(scheduleBuilder).build()
        SchedulerService.tasks.schedule(task = job.jobDetail, trigger = trigger)

        return TaskKey.Companion.fromJobKey(scheduler = SchedulerService.tasks.scheduler, jobKey = job.jobKey)
    }

    /**
     * Schedule the task based on the specified [Schedule].
     *
     * @param schedule The [Schedule] at which the task should be executed.
     */
    public fun send(schedule: Schedule): TaskKey {
        val job: BasicJob = buildJob()

        return when (schedule) {
            is Schedule.Interval -> send(job = job, interval = schedule)
            is Schedule.Cron -> send(job = job, cron = schedule.cron)
        }
    }

    /**
     * Schedule the task to be repeated at a specified [Schedule.Interval].
     *
     * @param job The job details and trigger builder for the task.
     * @param interval The [Schedule.Interval] at which the task should be repeated.
     *
     * @see [Schedule.Interval]
     */
    private fun send(job: BasicJob, interval: Schedule.Interval): TaskKey {
        // Define the schedule builder and set misfire instructions to
        // handle cases where the trigger misses its scheduled time,
        // in which case the task will be executed immediately.
        val scheduleBuilder: SimpleScheduleBuilder = SimpleScheduleBuilder.simpleSchedule()
            .withMisfireHandlingInstructionFireNow()

        // Apply repeat interval at which the task should repeat.
        val intervalInSeconds: UInt = interval.toTotalSeconds()
        if (intervalInSeconds > 0u) {
            scheduleBuilder.withIntervalInSeconds(intervalInSeconds.toInt())
            scheduleBuilder.repeatForever()
        }

        // When misfired reschedule to the next possible time. Only if the interval is set.
        scheduleBuilder.withMisfireHandlingInstructionNextWithExistingCount()

        // Send the task to the scheduler.
        val trigger: SimpleTrigger = job.triggerBuilder.withSchedule(scheduleBuilder).build()
        SchedulerService.tasks.schedule(task = job.jobDetail, trigger = trigger)

        return TaskKey.Companion.fromJobKey(scheduler = SchedulerService.tasks.scheduler, jobKey = job.jobKey)
    }

    /**
     * Schedule the task to be executed at a specified [Schedule.Cron] expression.
     *
     * @param job The job details and trigger builder for the task.
     * @param cron The [Schedule.Cron] expression at which the task should be executed.
     *
     * @see [Schedule.Cron]
     */
    private fun send(job: BasicJob, cron: String): TaskKey {
        val trigger: CronTrigger = job.triggerBuilder
            .withSchedule(CronScheduleBuilder.cronSchedule(cron))
            .build()

        // Send the task to the scheduler.
        SchedulerService.tasks.schedule(task = job.jobDetail, trigger = trigger)

        return TaskKey.Companion.fromJobKey(scheduler = SchedulerService.tasks.scheduler, jobKey = job.jobKey)
    }

    /**
     * Build the job details and trigger builder for the task.
     */
    private fun buildJob(): BasicJob {
        val groupName: String = groupId.toString()
        val jobKey: JobKey = JobKey.jobKey(taskId, groupName)
        val jobDataMap: JobDataMap = JobDataMap(parameters).apply {
            retryPolicy?.let { policy ->
                put(RetryPolicy.MAX_RETRIES_KEY, policy.maxRetries)
                put(RetryPolicy.COUNT_KEY, 0)
                when (val strategy: BackoffStrategy = policy.backoffStrategy) {
                    is BackoffStrategy.Fixed -> {
                        put(RetryPolicy.BACKOFF_TYPE_KEY, BackoffStrategy.FIXED_KEY)
                        put(RetryPolicy.DELAY_MS_KEY, strategy.delay.inWholeMilliseconds)
                    }

                    is BackoffStrategy.Exponential -> {
                        put(RetryPolicy.BACKOFF_TYPE_KEY, BackoffStrategy.EXPONENTIAL_KEY)
                        put(RetryPolicy.INITIAL_DELAY_MS_KEY, strategy.initialDelay.inWholeMilliseconds)
                        put(RetryPolicy.MULTIPLIER_KEY, strategy.multiplier)
                    }
                }
            }
        }

        val jobDetail: JobDetail = JobBuilder
            .newJob(consumerClass)
            .withIdentity(jobKey)
            .withDescription(parameters["DESCRIPTION"]?.toString())
            .usingJobData(jobDataMap)
            .storeDurably(true) // True to keep the job in the scheduler even after it is completed.
            .build()

        // Set the trigger name and start time based on task start configuration.
        val triggerBuilder: TriggerBuilder<Trigger> = TriggerBuilder.newTrigger()
            .withIdentity("$taskId-trigger", groupName)
            .apply {
                when (val startDateTime: TaskStartAt = startAt) {
                    is TaskStartAt.Immediate -> startNow()
                    is TaskStartAt.AtDateTime -> startDateTime.datetime.toJavaDate().let { startAt(it) }
                    is TaskStartAt.AfterDuration -> startDateTime.duration.toJavaInstant().let { startAt(Date.from((it))) }
                }
            }

        return BasicJob(jobKey = jobKey, jobDetail = jobDetail, triggerBuilder = triggerBuilder)
    }

    private data class BasicJob(
        val jobKey: JobKey,
        val jobDetail: JobDetail,
        val triggerBuilder: TriggerBuilder<Trigger>
    )

    public companion object {
        /**
         * Check if a group exists in the scheduler.
         *
         * @param groupId The Group ID to check.
         * @return True if the group exists, false otherwise.
         */
        public fun groupExists(groupId: Uuid): Boolean {
            return SchedulerService.tasks.exists(groupId = groupId)
        }

        /**
         * Retrieve all existing task keys for a specified group.
         *
         * @param groupId The Group ID to retrieve tasks for.
         * @return A list of [TaskKey] instances for the specified group.
         */
        public fun groupsTaskKeys(groupId: Uuid): List<TaskKey> {
            return SchedulerService.tasks.getTaskKeys(groupId = groupId)
        }

        /**
         * Delete a group and all associated tasks from the scheduler.
         *
         * @param groupId The Group ID to delete.
         * @return The number of tasks deleted.
         */
        public fun deleteGroup(groupId: Uuid): Int {
            return SchedulerService.tasks.delete(groupId = groupId)
        }
    }
}
