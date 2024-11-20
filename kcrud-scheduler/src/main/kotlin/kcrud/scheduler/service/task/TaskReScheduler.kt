/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.scheduler.service.task

import kcrud.core.event.SseService
import kcrud.core.util.DateTimeUtils.current
import kcrud.core.util.DateTimeUtils.formatted
import kcrud.core.util.DateTimeUtils.toJavaDate
import kcrud.core.util.DateTimeUtils.toJavaInstant
import kcrud.scheduler.service.schedule.TaskStartAt
import kotlinx.datetime.LocalDateTime
import org.quartz.*
import java.util.*

/**
 * Class to reschedule a task based on a new start time.
 *
 * #### Attention
 * This class is designed to work with one-time tasks only.
 * Recurring tasks are left unaffected to prevent multiple triggers.
 *
 * @property scheduler The Quartz scheduler.
 * @property jobDetail The job details.
 * @property startAt The new schedule time.
 */
internal class TaskReScheduler(
    private val scheduler: Scheduler,
    private val jobDetail: JobDetail,
    private val startAt: TaskStartAt
) {
    /**
     * Reschedules the task **only if it's a one-time task**.
     * Recurring tasks are left unaffected to prevent multiple triggers.
     */
    fun reschedule() {
        val jobKey: JobKey = jobDetail.key

        // Retrieve all triggers associated with this job.
        // Needed to determine if the task is recurring or one-time.
        val triggers: List<Trigger?>? = scheduler.getTriggersOfJob(jobKey)
        val primaryTrigger: Trigger? = triggers?.firstOrNull()

        if (primaryTrigger == null) {
            SseService.push(
                message = "${LocalDateTime.current().formatted(timeDelimiter = " | ", precision = 6)} " +
                        "| No trigger found for task '${jobKey.name}' " +
                        "| Group Id: ${jobKey.group} " +
                        "| Task Id: ${jobKey.name} "
            )
            return
        }

        // Determine if the task is recurring by checking the trigger type.
        val isRecurring: Boolean = when (primaryTrigger) {
            is SimpleTrigger -> primaryTrigger.repeatCount > 0 || primaryTrigger.repeatInterval > 0
            is CronTrigger -> true
            else -> false
        }

        // Ignore reschedule requests for recurring tasks.
        if (isRecurring) {
            SseService.push(
                message = "${LocalDateTime.current().formatted(timeDelimiter = " | ", precision = 6)} " +
                        "| Reschedule requested for recurring task '${jobKey.name}' is ignored. " +
                        "| Group Id: ${jobKey.group} " +
                        "| Task Id: ${jobKey.name} "
            )
            return
        }

        // For one-time tasks, create or update a re-schedule trigger.
        createOrUpdateRescheduleTrigger(jobKey = jobKey)
    }

    /**
     * Creates or updates a re-schedule trigger for one-time tasks.
     *
     * @param jobKey The job key for the task.
     */
    private fun createOrUpdateRescheduleTrigger(jobKey: JobKey) {
        // Define a unique trigger name for reschedules using the taskId.
        val rescheduleTriggerKey = TriggerKey("${jobKey.name}-reschedule", jobKey.group)

        // Create a new trigger based on TaskStartAt.
        val newTrigger: Trigger = TriggerBuilder.newTrigger()
            .withIdentity(rescheduleTriggerKey)
            .forJob(jobDetail)
            .apply {
                when (val startDateTime: TaskStartAt = startAt) {
                    is TaskStartAt.Immediate -> startNow()
                    is TaskStartAt.AtDateTime -> startDateTime.datetime.toJavaDate().let { startAt(it) }
                    is TaskStartAt.AfterDuration -> startDateTime.duration.toJavaInstant().let { startAt(Date.from((it))) }
                }
            }.build()

        // Check if a re-schedule trigger already exists.
        if (scheduler.checkExists(rescheduleTriggerKey)) {
            // Replace the existing re-schedule trigger.
            scheduler.rescheduleJob(rescheduleTriggerKey, newTrigger)
            SseService.push(
                message = "${LocalDateTime.current().formatted(timeDelimiter = " | ", precision = 6)} " +
                        "| Updated reschedule for one-time task '${jobKey.name}' " +
                        "| Group Id: ${jobKey.group} " +
                        "| Task Id: ${jobKey.name} " +
                        "| New Start At: $startAt"
            )
        } else {
            // Schedule a new reschedule trigger.
            scheduler.scheduleJob(newTrigger)
            SseService.push(
                message = "${LocalDateTime.current().formatted(timeDelimiter = " | ", precision = 6)} " +
                        "| Rescheduled one-time task '${jobKey.name}' " +
                        "| Group Id: ${jobKey.group} " +
                        "| Task Id: ${jobKey.name} " +
                        "| New Start At: $startAt"
            )
        }
    }
}
