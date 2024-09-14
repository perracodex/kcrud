/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.scheduler.service.core

import it.burning.cron.CronExpressionDescriptor
import kcrud.base.env.Tracer
import kcrud.base.events.SEEService
import kcrud.base.scheduler.annotation.SchedulerAPI
import kcrud.base.scheduler.audit.AuditService
import kcrud.base.scheduler.model.audit.AuditLog
import kcrud.base.scheduler.model.task.TaskSchedule
import kcrud.base.scheduler.model.task.TaskStateChange
import kcrud.base.scheduler.service.core.SchedulerTasks.Companion.create
import kcrud.base.scheduler.service.task.TaskState
import kcrud.base.security.snowflake.SnowflakeFactory
import kcrud.base.utils.DateTimeUtils
import org.quartz.*
import org.quartz.impl.matchers.GroupMatcher
import java.util.*
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlin.uuid.Uuid

/**
 * Helper class to manage tasks in the scheduler.
 *
 * Instances should be created using the [create] method in the companion object.
 * The constructor is private to prevent instantiation from outside modules and ensure
 * that only components annotated with [SchedulerAPI] can create instances.
 */
@OptIn(SchedulerAPI::class)
internal class SchedulerTasks private constructor(private val scheduler: Scheduler) {
    private val tracer = Tracer<SchedulerService>()

    /**
     * Schedules a new task with the given trigger.
     */
    fun schedule(task: JobDetail, trigger: Trigger) {
        tracer.debug("Scheduling new task: $task. Trigger: $trigger.")
        scheduler.scheduleJob(task, trigger)
    }

    /**
     * Pauses a concrete task currently scheduled in the scheduler.
     *
     * @param name The name of the task to pause.
     * @param group The group of the task to pause.
     * @return [TaskStateChange] containing details of the operation.
     */
    fun pause(name: String, group: String): TaskStateChange {
        return TaskState.change(scheduler = scheduler, targetState = Trigger.TriggerState.PAUSED) {
            val jobKey: JobKey = JobKey.jobKey(name, group)
            scheduler.pauseJob(JobKey.jobKey(name, group))
            TaskState.getTriggerState(scheduler = scheduler, jobKey = jobKey).name
        }
    }

    /**
     * Resumes a concrete task currently scheduled in the scheduler.
     *
     * @param name The name of the task to resume.
     * @param group The group of the task to resume.
     * @return [TaskStateChange] containing details of the operation.
     */
    fun resume(name: String, group: String): TaskStateChange {
        return TaskState.change(scheduler = scheduler, targetState = Trigger.TriggerState.NORMAL) {
            val jobKey: JobKey = JobKey.jobKey(name, group)
            scheduler.resumeJob(jobKey)
            TaskState.getTriggerState(scheduler = scheduler, jobKey = jobKey).name
        }
    }

    /**
     * Deletes a task from the scheduler.
     *
     * @param name The name of the task to be deleted.
     * @param group The group of the task to be deleted.
     * @return The number of tasks deleted.
     */
    fun delete(name: String, group: String): Int {
        tracer.debug("Deleting task. Name: $name. Group: $group.")
        return if (scheduler.deleteJob(JobKey.jobKey(name, group))) 1 else 0
    }

    /**
     * Deletes all tasks from the scheduler.
     *
     * @return The number of tasks deleted.
     */
    fun deleteAll(): Int {
        tracer.debug("Deleting all tasks.")
        return scheduler.getJobKeys(GroupMatcher.anyGroup()).count { jobKey ->
            scheduler.deleteJob(jobKey)
        }
    }

    /**
     * Returns a list of all task groups currently scheduled in the scheduler.
     */
    fun groups(): List<String> {
        return scheduler.jobGroupNames
    }

    /**
     * Re-trigger an existing durable job.
     *
     * @param name The name of the task to re-trigger.
     * @param group The group of the task to re-trigger.
     * @throws IllegalArgumentException If the task is not found.
     */
    suspend fun resend(name: String, group: String) {
        val jobKey: JobKey = JobKey.jobKey(name, group)

        // Triggers require a unique identity.
        val identity = "$name-trigger-resend-${Uuid.random()}"

        val triggerBuilder: TriggerBuilder<SimpleTrigger> = TriggerBuilder.newTrigger()
            .withIdentity(identity, group)
            .forJob(jobKey) // Associate with the existing durable job.
            .startNow()
            .withSchedule(
                SimpleScheduleBuilder.simpleSchedule()
                    .withMisfireHandlingInstructionIgnoreMisfires()
            )

        val newTrigger: Trigger = triggerBuilder.build()

        // Schedule the new trigger with the existing job.
        scheduler.scheduleJob(newTrigger)

        SEEService.push(
            "Task resent. Name: $name | Group: $group"
        )

        tracer.debug("Trigger for task ${jobKey.name} has been scheduled.")
    }

    /**
     * Returns a snapshot list of all tasks currently scheduled in the scheduler.
     *
     * @param groupId Optional group ID to filter the tasks by.
     * @param executing True if only actively executing tasks should be returned; false to return all tasks.
     * @param groupId The group ID of the tasks to return. Null to return all tasks.
     * @return A list of [TaskSchedule] objects representing the scheduled tasks.
     */
    suspend fun all(groupId: Uuid? = null, executing: Boolean = false): List<TaskSchedule> {
        var taskList: List<TaskSchedule> = if (executing) {
            scheduler.currentlyExecutingJobs.map { task -> toTaskSchedule(taskDetail = task.jobDetail) }
        } else {
            scheduler.getJobKeys(GroupMatcher.anyGroup()).mapNotNull { jobKey ->
                scheduler.getJobDetail(jobKey)?.let { detail -> toTaskSchedule(taskDetail = detail) }
            }
        }

        groupId?.let {
            taskList = taskList.filter { it.group == groupId.toString() }
        }

        // Sort the task list by nextFireTime.
        // Tasks without a nextFireTime will be placed at the end of the list.
        return taskList.sortedBy { it.nextFireTime }
    }

    /**
     * Helper method to create a [TaskSchedule] from a [JobDetail] including the next fire time.
     *
     * @param taskDetail The task detail from which to create the [TaskSchedule].
     * @return The constructed [TaskSchedule].
     */
    private suspend fun toTaskSchedule(taskDetail: JobDetail): TaskSchedule {
        val jobKey: JobKey = taskDetail.key
        val triggers: List<Trigger> = scheduler.getTriggersOfJob(jobKey)

        // Get the most restrictive state from the list of trigger states.
        val mostRestrictiveState: Trigger.TriggerState = TaskState.getTriggerState(
            scheduler = scheduler,
            taskDetail = taskDetail
        )

        // Resolve the last execution outcome.
        val mostRecentAudit: AuditLog? = AuditService.mostRecent(taskName = jobKey.name, taskGroup = jobKey.group)
        val outcome: String? = mostRecentAudit?.outcome?.name

        // Get how many times the task has been executed.
        val runs: Int = AuditService.count(taskName = jobKey.name, taskGroup = jobKey.group)

        // Resolve the schedule metrics.
        val (schedule: String?, scheduleInfo: String?) = triggers.firstOrNull()?.let { trigger ->
            when (trigger) {
                is SimpleTrigger -> {
                    val repeatInterval: Duration = trigger.repeatInterval.toDuration(unit = DurationUnit.MILLISECONDS)
                    if (repeatInterval.inWholeSeconds != 0L) "Every $repeatInterval" to null
                    else null to null
                }

                is CronTrigger -> {
                    CronExpressionDescriptor.getDescription(trigger.cronExpression) to trigger.cronExpression
                }

                else -> null to null
            }
        } ?: (null to null)

        // Resolve the concrete parameters of the task.
        val dataMap: List<String> = taskDetail.jobDataMap
            .entries.map { (key, value) -> "$key: $value" }
            .sorted()

        // Determine the next fire time of the task.
        val nextFireTime: Date? = triggers.mapNotNull { it.nextFireTime }.minOrNull()

        // Resolve the snowflake data.
        val snowflakeData: String = SnowflakeFactory.parse(id = jobKey.name).toString()

        return TaskSchedule(
            name = jobKey.name,
            snowflakeData = snowflakeData,
            group = jobKey.group,
            consumer = taskDetail.jobClass.simpleName,
            nextFireTime = nextFireTime?.let { DateTimeUtils.javaDateToLocalDateTime(datetime = it) },
            state = mostRestrictiveState.name,
            outcome = outcome,
            log = mostRecentAudit?.log,
            schedule = schedule,
            scheduleInfo = scheduleInfo,
            runs = runs,
            dataMap = dataMap,
        )
    }

    companion object {
        /**
         * Creates a new instance of [SchedulerTasks] with the provided [scheduler].
         *
         * @param scheduler The [Scheduler] instance to be used.
         * @return A new instance of [SchedulerTasks].
         */
        @SchedulerAPI
        internal fun create(scheduler: Scheduler): SchedulerTasks {
            return SchedulerTasks(scheduler = scheduler)
        }
    }
}
