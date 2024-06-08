/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.scheduler.service

import kcrud.base.persistence.serializers.SUUID
import kcrud.base.security.snowflake.SnowflakeFactory
import kcrud.base.utils.DateTimeUtils
import kcrud.base.utils.DateTimeUtils.toJavaDate
import kcrud.base.utils.DateTimeUtils.toJavaInstant
import org.quartz.*
import java.util.*

/**
 * Class to send a scheduling request for a task.
 * It supports both simple intervals and cron-based scheduling.
 *
 * @property taskId The ID of the task to be scheduled.
 * @property taskClass The class of the task to be scheduled.
 * @property startAt Specifies when the task should start. Defaults to immediate execution.
 * @property parameters Optional parameters to be passed to the task class.
 */
@Suppress("MemberVisibilityCanBePrivate", "unused")
class SchedulerRequest(
    val taskId: SUUID,
    val taskClass: Class<out SchedulerTask>,
    var startAt: TaskStartAt = TaskStartAt.Immediate,
    var parameters: Map<String, Any> = emptyMap()
) {
    /**
     * Schedule the task to be executed immediately or at a specified [startAt] time.
     */
    fun send(): JobKey {
        val job: BasicJob = buildJob()

        // Define the schedule builder and set misfire instructions to
        // handle cases where the trigger misses its scheduled time,
        // in which case the task will be executed immediately.
        val scheduleBuilder: SimpleScheduleBuilder = SimpleScheduleBuilder.simpleSchedule()
            .withMisfireHandlingInstructionFireNow()

        // Send the task to the scheduler.
        val trigger: SimpleTrigger = job.triggerBuilder.withSchedule(scheduleBuilder).build()
        SchedulerService.newTask(task = job.jobDetail, trigger = trigger)

        return job.jobKey
    }

    /**
     * Schedule the task to be repeated at specified intervals.
     *
     * @param interval The interval at which the task should be repeated.
     */
    fun send(interval: DateTimeUtils.Interval): JobKey {
        val job: BasicJob = buildJob()

        // Define the schedule builder and set misfire instructions to
        // handle cases where the trigger misses its scheduled time,
        // in which case the task will be executed immediately.
        val scheduleBuilder: SimpleScheduleBuilder = SimpleScheduleBuilder.simpleSchedule()
            .withMisfireHandlingInstructionFireNow()

        // Apply repeat interval at which the task should repeat.
        interval.let {
            val intervalInMinutes: UInt = it.toTotalMinutes()
            if (intervalInMinutes > 0u) {
                scheduleBuilder.withIntervalInMinutes(intervalInMinutes.toInt())
                scheduleBuilder.repeatForever()
            }

            // When misfired reschedule to the next possible time. Only if the interval is set.
            scheduleBuilder.withMisfireHandlingInstructionNextWithExistingCount()
        }

        // Send the task to the scheduler.
        val trigger: SimpleTrigger = job.triggerBuilder.withSchedule(scheduleBuilder).build()
        SchedulerService.newTask(task = job.jobDetail, trigger = trigger)

        return job.jobKey
    }

    /**
     * Schedule the task to be executed at a specified cron expression.
     *
     * The cron expression is composed of the following fields:
     * ```
     * ┌───────────── second (0-59)
     * │ ┌───────────── minute (0-59)
     * │ │ ┌───────────── hour (0-23)
     * │ │ │ ┌───────────── day of month (1-31)
     * │ │ │ │ ┌───────────── month (1-12 or JAN-DEC)
     * │ │ │ │ │ ┌───────────── day of week (0-7, SUN-SAT. Both 0 & 7 = Sunday)
     * │ │ │ │ │ │ ┌───────────── year (optional)
     * │ │ │ │ │ │ │
     * │ │ │ │ │ │ │
     * * * * * * * *
     * ```
     *
     * ```
     * Sample cron expressions:
     *   - "0 0 0 * * ?" - At midnight every day.
     *   - "0 0 12 ? * MON-FRI" - At noon every weekday.
     *   - "0 0/30 9-17 * * ?" - Every 30 minutes between 9 AM to 5 PM.
     *   - "0 0 0 1 * ?" - At midnight on the first day of every month.
     *   - "0 0 6 ? * SUN" - At 6 AM every Sunday.
     *   - "0 0 14 * * ?" - At 2 PM every day.
     *   - "0 15 10 ? * *" - At 10:15 AM every day.
     *   - "0 0/15 * * * ?" - Every 15 minutes.
     *   - "0 0 0 ? * MON#1" - At midnight on the first Monday of every month.
     *   - "30 0 0 * * ?" - At 00:00:30 (30 seconds past midnight) every day.
     *   - "0 * * * * ?" - Every minute.
     * ```
     *
     * @param cron The cron expression at which the task should be executed.
     */
    @Suppress("unused")
    fun send(cron: String): JobKey {
        val job: BasicJob = buildJob()

        val trigger: CronTrigger = job.triggerBuilder
            .withSchedule(CronScheduleBuilder.cronSchedule(cron))
            .build()

        // Send the task to the scheduler.
        SchedulerService.newTask(task = job.jobDetail, trigger = trigger)

        return job.jobKey
    }

    /**
     * Build the job details and trigger builder for the task.
     */
    private fun buildJob(): BasicJob {
        val snowflake: String = SnowflakeFactory.nextId()
        val taskName = "task-${snowflake}-${System.nanoTime()}"
        val groupName: String = taskId.toString()

        val jobKey: JobKey = JobKey.jobKey(taskName, groupName)
        val jobDataMap = JobDataMap(parameters)

        val jobDetail: JobDetail = JobBuilder
            .newJob(taskClass)
            .withIdentity(jobKey)
            .usingJobData(jobDataMap)
            .build()

        // Set the trigger name and start time based on task start configuration.
        val triggerBuilder: TriggerBuilder<Trigger> = TriggerBuilder.newTrigger()
            .withIdentity("${taskName}-trigger", groupName)
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
}
