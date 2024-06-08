/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.scheduler.service.core

import io.ktor.server.application.*
import kcrud.base.env.Tracer
import kcrud.base.scheduler.annotation.SchedulerAPI
import kcrud.base.scheduler.entity.TaskScheduleEntity
import kcrud.base.scheduler.entity.TaskStateChangeEntity
import kcrud.base.scheduler.listener.TaskListener
import kcrud.base.scheduler.listener.TaskTriggerListener
import kcrud.base.scheduler.service.task.TaskFactory
import kcrud.base.scheduler.service.task.TaskState
import kcrud.base.utils.DateTimeUtils
import org.quartz.*
import org.quartz.Trigger.TriggerState
import org.quartz.impl.StdSchedulerFactory
import org.quartz.impl.matchers.GroupMatcher
import java.util.*
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * Core task scheduler service that manages the scheduling and execution of tasks.
 *
 * See: [Quartz Scheduler Documentation](https://github.com/quartz-scheduler/quartz/blob/main/docs/index.adoc)
 *
 * See: [Quartz Scheduler Configuration](https://www.quartz-scheduler.org/documentation/2.4.0-SNAPSHOT/configuration.html)
 */
@OptIn(SchedulerAPI::class)
object SchedulerService {
    private val tracer = Tracer<SchedulerService>()

    /** The key used to store the application settings in the task data map. */
    const val APP_SETTINGS_KEY: String = "APP_SETTINGS"

    private val scheduler: Scheduler

    init {
        tracer.debug("Configuring the task scheduler.")

        val schema: Properties = SchedulerConfig.get()
        val schedulerFactory: SchedulerFactory = StdSchedulerFactory(schema)

        scheduler = schedulerFactory.scheduler
        scheduler.setJobFactory(TaskFactory())

        tracer.debug("Task scheduler configured.")
    }

    /**
     * Starts the task scheduler.
     */
    fun startScheduler() {
        tracer.info("Starting task scheduler.")
        scheduler.listenerManager.addJobListener(TaskListener())
        scheduler.listenerManager.addTriggerListener(TaskTriggerListener())
        scheduler.start()
        tracer.info("Task scheduler started.")
    }

    /**
     * Configures the task scheduler to shut down when the application is stopped.
     */
    fun configureScheduler(environment: ApplicationEnvironment) {
        // Add a shutdown hook to stop the scheduler when the application is stopped.
        environment.monitor.subscribe(ApplicationStopping) {
            tracer.info("Shutting down task scheduler.")
            scheduler.shutdown()
            tracer.info("Task scheduler shut down.")
        }
    }

    /**
     * Stops the task scheduler.
     */
    fun stopScheduler() {
        tracer.info("Stopping task scheduler.")
        scheduler.shutdown()
    }

    /**
     * Schedules a new task with the given trigger.
     */
    fun newTask(task: JobDetail, trigger: Trigger) {
        tracer.debug("Scheduling new task: $task. Trigger: $trigger.")
        scheduler.scheduleJob(task, trigger)
    }

    /**
     * Deletes a task from the scheduler.
     *
     * @param name The name of the task to be deleted.
     * @param group The group of the task to be deleted.
     * @return The number of tasks deleted.
     */
    fun deleteTask(name: String, group: String): Int {
        tracer.debug("Deleting task. Name: $name. Group: $group.")
        return if (scheduler.deleteJob(JobKey.jobKey(name, group))) 1 else 0
    }

    /**
     * Deletes all tasks from the scheduler.
     *
     * @return The number of tasks deleted.
     */
    fun deleteAllTasks(): Int {
        tracer.debug("Deleting all tasks.")
        return scheduler.getJobKeys(GroupMatcher.anyGroup()).count { jobKey ->
            scheduler.deleteJob(jobKey)
        }
    }

    /**
     * Returns a list of all task groups currently scheduled in the scheduler.
     */
    fun getGroups(): List<String> {
        return scheduler.jobGroupNames
    }

    /**
     * Returns a snapshot list of all tasks currently scheduled in the scheduler.
     *
     * @param executing True if only actively executing tasks should be returned; false to return all tasks.
     * @return A list of [TaskScheduleEntity] objects representing the scheduled tasks.
     */
    fun getTasks(executing: Boolean = false): List<TaskScheduleEntity> {
        val taskList: List<TaskScheduleEntity> = if (executing) {
            scheduler.currentlyExecutingJobs.map { task -> createTaskScheduleEntity(taskDetail = task.jobDetail) }
        } else {
            scheduler.getJobKeys(GroupMatcher.anyGroup()).mapNotNull { jobKey ->
                scheduler.getJobDetail(jobKey)?.let { detail -> createTaskScheduleEntity(taskDetail = detail) }
            }
        }

        // Sort the task list by nextFireTime.
        // Tasks without a nextFireTime will be placed at the end of the list.
        return taskList.sortedBy { it.nextFireTime }
    }

    /**
     * Pauses all tasks currently scheduled in the scheduler.
     *
     * @return [TaskStateChangeEntity] containing details of the operation.
     */
    fun pauseScheduler(): TaskStateChangeEntity {
        return TaskState.change(scheduler = scheduler, targetState = TriggerState.PAUSED) {
            scheduler.pauseAll()
        }
    }

    /**
     * Pauses a concrete task currently scheduled in the scheduler.
     *
     * @param name The name of the task to pause.
     * @param group The group of the task to pause.
     * @return [TaskStateChangeEntity] containing details of the operation.
     */
    fun pauseTask(name: String, group: String): TaskStateChangeEntity {
        return TaskState.change(scheduler = scheduler, targetState = TriggerState.PAUSED) {
            scheduler.pauseJob(JobKey.jobKey(name, group))
        }
    }

    /**
     * Resumes all tasks currently paused in the scheduler.
     *
     * @return [TaskStateChangeEntity] containing details of the operation.
     */
    fun resumeScheduler(): TaskStateChangeEntity {
        return TaskState.change(scheduler = scheduler, targetState = TriggerState.NORMAL) {
            scheduler.resumeAll()
        }
    }

    /**
     * Resumes a concrete task currently scheduled in the scheduler.
     *
     * @param name The name of the task to resume.
     * @param group The group of the task to resume.
     * @return [TaskStateChangeEntity] containing details of the operation.
     */
    fun resumeTask(name: String, group: String): TaskStateChangeEntity {
        return TaskState.change(scheduler = scheduler, targetState = TriggerState.NORMAL) {
            scheduler.resumeJob(JobKey.jobKey(name, group))
        }
    }

    /**
     * Helper method to create a [TaskScheduleEntity] from a [JobDetail] including the next fire time.
     *
     * @param taskDetail The task detail from which to create the [TaskScheduleEntity].
     * @return The constructed [TaskScheduleEntity].
     */
    private fun createTaskScheduleEntity(taskDetail: JobDetail): TaskScheduleEntity {
        val jobKey: JobKey = taskDetail.key
        val triggers: List<Trigger> = scheduler.getTriggersOfJob(jobKey)
        val nextFireTime: Date? = triggers.mapNotNull { it.nextFireTime }.minOrNull()

        // Get the most restrictive state from the list of trigger states.
        val triggerStates: List<TriggerState> = triggers.map { scheduler.getTriggerState(it.key) }
        val mostRestrictiveState: TriggerState = when {
            triggerStates.any { it == TriggerState.PAUSED } -> TriggerState.PAUSED
            triggerStates.any { it == TriggerState.BLOCKED } -> TriggerState.BLOCKED
            triggerStates.any { it == TriggerState.ERROR } -> TriggerState.ERROR
            triggerStates.any { it == TriggerState.COMPLETE } -> TriggerState.COMPLETE
            else -> TriggerState.NORMAL  // Assuming NORMAL as default if no other states are found.
        }

        // Resolve the interval metrics.
        val (interval, runs) = triggers.firstOrNull()?.let { trigger ->
            if (trigger is SimpleTrigger) {
                val repeatInterval: Duration = trigger.repeatInterval.toDuration(unit = DurationUnit.MILLISECONDS)
                val totalMinutes: Long = repeatInterval.inWholeMinutes

                if (totalMinutes != 0L) {
                    DateTimeUtils.formatDuration(duration = repeatInterval) to trigger.timesTriggered
                } else null
            } else if (trigger is CronTrigger) {
                trigger.cronExpression to null
            } else null
        } ?: (null to null)

        return TaskScheduleEntity(
            name = jobKey.name,
            group = jobKey.group,
            className = taskDetail.jobClass.simpleName,
            nextFireTime = nextFireTime?.let { DateTimeUtils.javaDateToLocalDateTime(datetime = it) },
            state = mostRestrictiveState.name,
            interval = interval,
            runs = runs,
            dataMap = taskDetail.jobDataMap.toList().toString(),
        )
    }
}
