/*
 * Copyright (c) 2024-Present Perraco. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.scheduling.entity

import kcrud.base.scheduling.service.JobSchedulerService
import kcrud.base.scheduling.service.JobStartAt
import kcrud.base.utils.DateTimeUtils.toInstant
import kcrud.base.utils.DateTimeUtils.toJavaDate
import org.quartz.*

/**
 * Data class to build a new job schedule request.
 *
 * @param jobClass The class of the job to be scheduled.
 * @param jobName The name of the job.
 * @param groupName The group of the job.
 * @param startAt The time at which the job should start.
 * @param repeatIntervalInSeconds The interval at which the job should repeat.
 * @param repeatCount The number of times the job should repeat.
 * @param parameters Optional parameters to be passed to the job class.
 */
data class JobScheduleRequest(
    val jobClass: Class<out Job>,
    val jobName: String = "job_${System.nanoTime()}",
    var groupName: String = "DefaultGroup",
    var startAt: JobStartAt = JobStartAt.Now,
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

            val triggerBuilder: TriggerBuilder<Trigger> = TriggerBuilder.newTrigger()
                .withIdentity("${config.jobName}_trigger", config.groupName)
                .apply {
                    when (val startAt = config.startAt) {
                        is JobStartAt.Now -> startNow()
                        is JobStartAt.AtDate -> startAt.date.toJavaDate()
                        is JobStartAt.AfterDuration -> startAt.duration.toInstant()
                    }
                }

            val trigger: Trigger = config.repeatIntervalInSeconds?.let { interval ->
                val scheduleBuilder = SimpleScheduleBuilder.simpleSchedule()
                    .withIntervalInSeconds(interval)
                    .apply {
                        config.repeatCount?.let { repeatCount ->
                            withRepeatCount(repeatCount)
                        } ?: repeatForever()
                    }
                triggerBuilder.withSchedule(scheduleBuilder).build()
            } ?: triggerBuilder.build()

            JobSchedulerService.newJob(job = jobDetail, trigger = trigger)

            return jobKey
        }
    }
}