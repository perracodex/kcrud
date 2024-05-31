/*
 * Copyright (c) 2024-Present Perracodex. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.scheduling.entity

import kcrud.base.scheduling.service.JobSchedulerService
import kcrud.base.scheduling.service.JobStartAt
import kcrud.base.utils.DateTimeUtils.toJavaDate
import kcrud.base.utils.DateTimeUtils.toJavaInstant
import org.quartz.*
import java.util.*

/**
 * Data class to build a new job schedule request.
 *
 * @property jobClass The class of the job to be scheduled.
 * @property jobName The name of the job.
 * @property groupName The group of the job.
 * @property startAt The time at which the job should start.
 * @property repeatIntervalInSeconds The interval at which the job should repeat.
 * @property repeatCount The number of times the job should repeat.
 * @property parameters Optional parameters to be passed to the job class.
 */
data class JobScheduleRequest(
    val jobClass: Class<out Job>,
    val jobName: String = "job_${System.nanoTime()}",
    var groupName: String = "DefaultGroup",
    var startAt: JobStartAt = JobStartAt.Immediate,
    var repeatIntervalInSeconds: Int? = null,
    var repeatCount: Int? = null,
    var parameters: Map<String, Any> = emptyMap()
) {
    companion object {
        /**
         * Creates a new job schedule request.
         *
         * @param jobClass The class of the job to be scheduled.
         * @param configure The configuration for the job schedule request.
         * @return The [JobKey] of the scheduled job.
         */
        fun send(jobClass: Class<out Job>, configure: JobScheduleRequest.() -> Unit): JobKey {
            val config = JobScheduleRequest(jobClass).apply(configure)
            val jobKey: JobKey = JobKey.jobKey(config.jobName, config.groupName)
            val jobDataMap = JobDataMap(config.parameters)

            val jobDetail: JobDetail = JobBuilder
                .newJob(config.jobClass)
                .withIdentity(jobKey)
                .usingJobData(jobDataMap)
                .build()

            // Set the trigger name and start time based on job start configuration.
            val triggerBuilder: TriggerBuilder<Trigger> = TriggerBuilder.newTrigger()
                .withIdentity("${config.jobName}_trigger", config.groupName)
                .apply {
                    when (val startDateTime: JobStartAt = config.startAt) {
                        is JobStartAt.Immediate -> startNow()
                        is JobStartAt.AtDateTime -> startDateTime.datetime.toJavaDate().let { startAt(it) }
                        is JobStartAt.AfterDuration -> startDateTime.duration.toJavaInstant().let { startAt(Date.from((it))) }
                    }
                }

            // Define the schedule builder and set misfire instructions
            // to handle cases where the trigger misses its scheduled time,
            // in which case the job will be executed immediately.
            val scheduleBuilder = SimpleScheduleBuilder.simpleSchedule()
                .withMisfireHandlingInstructionFireNow()

            // Apply repeat interval and count if specified.
            // If repeat count is null, set to repeat forever.
            config.repeatIntervalInSeconds?.let { repeatIntervalInSeconds ->
                scheduleBuilder.withIntervalInSeconds(repeatIntervalInSeconds)
                config.repeatCount?.let { repeatCount ->
                    scheduleBuilder.withRepeatCount(repeatCount)
                } ?: scheduleBuilder.repeatForever()
            }

            // Build the trigger with the schedule
            val trigger: SimpleTrigger = triggerBuilder.withSchedule(scheduleBuilder).build() as SimpleTrigger
            JobSchedulerService.newJob(job = jobDetail, trigger = trigger)

            return jobKey
        }
    }
}