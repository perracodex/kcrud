/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.core.scheduler.service

import it.burning.cron.CronExpressionDescriptor
import kcrud.core.env.Tracer
import kcrud.core.event.SseService
import kcrud.core.scheduler.audit.AuditService
import kcrud.core.scheduler.model.audit.AuditLog
import kcrud.core.scheduler.model.task.TaskSchedule
import kcrud.core.scheduler.model.task.TaskStateChange
import kcrud.core.scheduler.service.SchedulerTasks.Companion.create
import kcrud.core.scheduler.service.annotation.SchedulerApi
import kcrud.core.scheduler.service.task.TaskState
import kcrud.core.security.snowflake.SnowflakeFactory
import kcrud.core.util.DateTimeUtils.toKotlinLocalDateTime
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
 * that only components annotated with [SchedulerApi] can create instances.
 */
@OptIn(SchedulerApi::class)
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
     * @param groupId The group of the task to pause.
     * @param taskId The name of the task to pause, or null to pause all tasks in the group.
     * @return [TaskStateChange] containing details of the operation.
     */
    fun pause(groupId: String, taskId: String?): TaskStateChange {
        if (taskId.isNullOrBlank()) {
            return pause(groupId = groupId)
        }

        tracer.debug("Pausing task.Group: $groupId. Task: $taskId.")
        return TaskState.change(scheduler = scheduler, targetState = Trigger.TriggerState.PAUSED) {
            val jobKey: JobKey = JobKey.jobKey(taskId, groupId)
            scheduler.pauseJob(JobKey.jobKey(taskId, groupId))
            TaskState.getTriggerState(scheduler = scheduler, jobKey = jobKey).name
        }
    }

    /**
     * Pauses all tasks within the specified group in the scheduler.
     *
     * @param groupId The group ID of the tasks to be paused.
     * @return [TaskStateChange] containing details of the operation.
     */
    fun pause(groupId: String): TaskStateChange {
        tracer.debug("Pausing all tasks in group: $groupId.")
        val jobKeys: Set<JobKey?>? = scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupId))
        return TaskState.change(scheduler = scheduler, targetState = Trigger.TriggerState.PAUSED) {
            scheduler.pauseJobs(GroupMatcher.jobGroupEquals(groupId))
            // Return the most restrictive state among paused tasks.
            jobKeys?.filterNotNull()
                ?.map { TaskState.getTriggerState(scheduler = scheduler, jobKey = it) }
                ?.minByOrNull { it.ordinal }?.name ?: ""
        }
    }

    /**
     * Resumes a concrete task currently scheduled in the scheduler.
     *
     * @param groupId The group of the task to resume.
     * @param taskId The name of the task to resume, or null to resume all tasks in the group.
     * @return [TaskStateChange] containing details of the operation.
     */
    fun resume(groupId: String, taskId: String?): TaskStateChange {
        if (taskId.isNullOrBlank()) {
            return resume(groupId = groupId)
        }

        tracer.debug("Resuming task.Group: $groupId. Task: $taskId.")
        return TaskState.change(scheduler = scheduler, targetState = Trigger.TriggerState.NORMAL) {
            val jobKey: JobKey = JobKey.jobKey(taskId, groupId)
            scheduler.resumeJob(jobKey)
            TaskState.getTriggerState(scheduler = scheduler, jobKey = jobKey).name
        }
    }

    /**
     * Resumes all tasks within the specified group in the scheduler.
     *
     * @param groupId The group ID of the tasks to be resumed.
     * @return [TaskStateChange] containing details of the operation.
     */
    fun resume(groupId: String): TaskStateChange {
        tracer.debug("Resuming all tasks in group: $groupId.")
        val jobKeys: Set<JobKey?>? = scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupId))
        return TaskState.change(scheduler = scheduler, targetState = Trigger.TriggerState.NORMAL) {
            scheduler.resumeJobs(GroupMatcher.jobGroupEquals(groupId))
            // Return the most restrictive state among resumed tasks.
            jobKeys?.filterNotNull()
                ?.map { TaskState.getTriggerState(scheduler = scheduler, jobKey = it) }
                ?.minByOrNull { it.ordinal }?.name ?: ""
        }
    }

    /**
     * Deletes a task from the scheduler.
     *
     * @param groupId The group of the task to be deleted.
     * @param taskId The id of the task to be deleted.
     * @return The number of tasks deleted.
     */
    fun delete(groupId: String, taskId: String): Int {
        tracer.debug("Deleting task.Group: $groupId. Task: $taskId. ")
        return if (scheduler.deleteJob(JobKey.jobKey(taskId, groupId))) 1 else 0
    }

    /**
     * Deletes all tasks within the specified group from the scheduler.
     *
     * @param groupId The group ID of the tasks to be deleted.
     * @return The number of tasks deleted.
     */
    fun delete(groupId: String): Int {
        tracer.debug("Deleting all tasks in group: $groupId.")
        val jobKeys: Set<JobKey?>? = scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupId))
        var deletedCount = 0

        jobKeys?.forEach { jobKey ->
            if (scheduler.deleteJob(jobKey)) {
                deletedCount++
            }
        }

        tracer.debug("Deleted $deletedCount tasks from group: $groupId.")
        return deletedCount
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
     * @param groupId The group of the task to re-trigger.
     * @param taskId The name of the task to re-trigger.
     * @throws IllegalArgumentException If the task is not found.
     */
    fun resend(groupId: String, taskId: String) {
        val jobKey: JobKey = JobKey.jobKey(taskId, groupId)

        // Triggers require a unique identity.
        val identity = "$taskId-trigger-resend-${Uuid.random()}"

        val triggerBuilder: TriggerBuilder<SimpleTrigger> = TriggerBuilder.newTrigger()
            .withIdentity(identity, groupId)
            .forJob(jobKey) // Associate with the existing durable job.
            .startNow()
            .withSchedule(
                SimpleScheduleBuilder.simpleSchedule()
                    .withMisfireHandlingInstructionIgnoreMisfires()
            )

        val newTrigger: Trigger = triggerBuilder.build()

        // Schedule the new trigger with the existing job.
        scheduler.scheduleJob(newTrigger)

        SseService.push(message = "Task resent. Group: $groupId | Task: $taskId")
        tracer.debug("Trigger for task ${jobKey.name} has been scheduled.")
    }

    /**
     * Re-triggers all tasks within the specified group in the scheduler.
     *
     * @param groupId The group ID of the tasks to re-trigger.
     */
    fun resend(groupId: String) {
        tracer.debug("Resending all tasks in group: $groupId.")
        val jobKeys: Set<JobKey?>? = scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupId))
        jobKeys?.forEach { jobKey ->
            jobKey?.let {
                resend(groupId = groupId, taskId = jobKey.name)
            }
        }
        SseService.push(message = "All tasks in group '$groupId' have been resent.")
    }

    /**
     * Returns a snapshot list of all tasks currently scheduled in the scheduler.
     *
     * @param groupId Optional group ID to filter the tasks by.
     * @param executing True if only actively executing tasks should be returned; false to return all tasks.
     * @param groupId The group ID of the tasks to return. Null to return all tasks.
     * @param sortByFireTime True if the tasks should be sorted by nextFireTime; false to return them in the order they were scheduled.
     * @return A list of [TaskSchedule] objects representing the scheduled tasks.
     */
    suspend fun all(groupId: Uuid? = null, executing: Boolean = false, sortByFireTime: Boolean = false): List<TaskSchedule> {
        var taskList: List<TaskSchedule> = if (executing) {
            scheduler.currentlyExecutingJobs.map { task -> toTaskSchedule(taskDetail = task.jobDetail) }
        } else {
            scheduler.getJobKeys(GroupMatcher.anyGroup()).mapNotNull { jobKey ->
                scheduler.getJobDetail(jobKey)?.let { detail -> toTaskSchedule(taskDetail = detail) }
            }
        }

        groupId?.let {
            taskList = taskList.filter { it.groupId == groupId.toString() }
        }

        // Sort the task list by nextFireTime.
        // Tasks without a nextFireTime will be placed at the end of the list.
        return if (sortByFireTime) {
            taskList.sortedBy { it.nextFireTime }
        } else {
            taskList
        }
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
        val mostRecentAudit: AuditLog? = AuditService.mostRecent(groupId = jobKey.group, taskId = jobKey.name)
        val outcome: String? = mostRecentAudit?.outcome?.name

        // Get how many times the task has been executed.
        val runs: Int = AuditService.count(groupId = jobKey.group, taskId = jobKey.name)

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
            groupId = jobKey.group,
            taskId = jobKey.name,
            snowflakeData = snowflakeData,
            consumer = taskDetail.jobClass.simpleName,
            nextFireTime = nextFireTime?.toKotlinLocalDateTime(),
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
        @SchedulerApi
        internal fun create(scheduler: Scheduler): SchedulerTasks {
            return SchedulerTasks(scheduler = scheduler)
        }
    }
}
