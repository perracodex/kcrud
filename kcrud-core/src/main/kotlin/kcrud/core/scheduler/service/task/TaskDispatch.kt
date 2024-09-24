/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.core.scheduler.service.task

import kcrud.core.scheduler.service.SchedulerService
import kcrud.core.scheduler.service.annotation.SchedulerAPI
import kcrud.core.scheduler.service.schedule.Schedule
import kcrud.core.scheduler.service.schedule.TaskStartAt
import kcrud.core.security.snowflake.SnowflakeFactory
import kcrud.core.utils.DateTimeUtils.toJavaDate
import kcrud.core.utils.DateTimeUtils.toJavaInstant
import org.quartz.*
import java.util.*
import kotlin.uuid.Uuid

/**
 * Class to create and send a scheduling request for a task.
 * It supports both simple intervals and cron-based scheduling.
 *
 * @property taskId The ID of the task to be scheduled.
 * @property consumerClass The class of the task consumer to be scheduled.
 * @property startAt Specifies when the task should start. Defaults to immediate execution.
 * @property parameters Optional parameters to be passed to the task class.
 */
@OptIn(SchedulerAPI::class)
public class TaskDispatch(
    private val taskId: Uuid,
    private val consumerClass: Class<out TaskConsumer>,
    private var startAt: TaskStartAt = TaskStartAt.Immediate,
    private var parameters: Map<String, Any?> = emptyMap()
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

        return TaskKey.fromJobKey(job.jobKey)
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

        return TaskKey.fromJobKey(job.jobKey)
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

        return TaskKey.fromJobKey(job.jobKey)
    }

    /**
     * Build the job details and trigger builder for the task.
     */
    private fun buildJob(): BasicJob {
        val taskName: String = SnowflakeFactory.nextId()
        val groupName: String = taskId.toString()

        val jobKey: JobKey = JobKey.jobKey(taskName, groupName)
        val jobDataMap = JobDataMap(parameters)

        val jobDetail: JobDetail = JobBuilder
            .newJob(consumerClass)
            .withIdentity(jobKey)
            .usingJobData(jobDataMap)
            .storeDurably(true) // True to keep the job in the scheduler even after it is completed.
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
