/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.scheduler.service

import kcrud.base.persistence.serializers.SUUID
import kcrud.base.security.snowflake.SnowflakeFactory
import kcrud.base.utils.DateTimeUtils.toJavaDate
import kcrud.base.utils.DateTimeUtils.toJavaInstant
import org.quartz.*
import java.util.*

/**
 * Data class to build a new task schedule request.
 *
 * @property taskClass The class of the task to be scheduled.
 * @property startAt The time at which the task should start.
 * @property interval Optional interval at which the task should repeat. In Minutes.
 * @property parameters Optional parameters to be passed to the task class.
 */
data class SchedulerRequest(
    var taskClass: Class<out SchedulerTask>,
    var startAt: TaskStartAt = TaskStartAt.Immediate,
    var interval: UInt? = null,
    var parameters: Map<String, Any> = emptyMap()
) {
    companion object {
        /**
         * Creates a new task schedule request.
         *
         * @param taskId The ID of the task to be scheduled.
         * @param taskClass The class of the task to be scheduled.
         * @param configuration The configuration for the task schedule request.
         * @return The [JobKey] of the scheduled task.
         */
        fun send(taskId: SUUID, taskClass: Class<out SchedulerTask>, configuration: SchedulerRequest.() -> Unit): JobKey {
            val snowflake: String = SnowflakeFactory.nextId()
            val taskName = "task-${snowflake}-${System.nanoTime()}"
            val groupName: String = taskId.toString()

            val config: SchedulerRequest = SchedulerRequest(
                taskClass = taskClass
            ).apply(configuration)

            val jobKey: JobKey = JobKey.jobKey(taskName, groupName)
            val jobDataMap = JobDataMap(config.parameters)

            val jobDetail: JobDetail = JobBuilder
                .newJob(config.taskClass)
                .withIdentity(jobKey)
                .usingJobData(jobDataMap)
                .build()

            // Set the trigger name and start time based on task start configuration.
            val triggerBuilder: TriggerBuilder<Trigger> = TriggerBuilder.newTrigger()
                .withIdentity("${taskName}-trigger", groupName)
                .apply {
                    when (val startDateTime: TaskStartAt = config.startAt) {
                        is TaskStartAt.Immediate -> startNow()
                        is TaskStartAt.AtDateTime -> startDateTime.datetime.toJavaDate().let { startAt(it) }
                        is TaskStartAt.AfterDuration -> startDateTime.duration.toJavaInstant().let { startAt(Date.from((it))) }
                    }
                }

            // Define the schedule builder and set misfire instructions to
            // handle cases where the trigger misses its scheduled time,
            // in which case the task will be executed immediately.
            val scheduleBuilder: SimpleScheduleBuilder = SimpleScheduleBuilder.simpleSchedule()
                .withMisfireHandlingInstructionFireNow()

            // Apply repeat interval at which the task should repeat.
            config.interval?.let { interval ->
                scheduleBuilder.withIntervalInMinutes(interval.toInt())
                scheduleBuilder.repeatForever()
            }

            // When misfired reschedule to the next possible time.
            scheduleBuilder.withMisfireHandlingInstructionNextWithExistingCount()

            // Build the trigger with the schedule
            val trigger: SimpleTrigger = triggerBuilder.withSchedule(scheduleBuilder).build() as SimpleTrigger
            SchedulerService.newTask(task = jobDetail, trigger = trigger)

            return jobKey
        }
    }
}
