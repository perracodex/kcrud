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

    enum class TaskSchedulerState {
        RUNNING,
        PAUSED,
        STOPPED
    }

    /** The key used to store the application settings in the task data map. */
    const val APP_SETTINGS_KEY: String = "APP_SETTINGS"

    /** Scheduler instance used to manage tasks. */
    private lateinit var scheduler: Scheduler

    /** Manage tasks in the scheduler. */
    lateinit var tasks: Tasks
        private set

    /**
     * Configures the task scheduler.
     */
    private fun setup() {
        tracer.debug("Configuring the task scheduler.")

        val schema: Properties = SchedulerConfig.get()
        val schedulerFactory: SchedulerFactory = StdSchedulerFactory(schema)

        scheduler = schedulerFactory.scheduler
        scheduler.setJobFactory(TaskFactory())

        tasks = Tasks.create(scheduler = scheduler)

        tracer.debug("Task scheduler configured.")
    }

    /**
     * Starts the task scheduler.
     */
    fun start() {
        if (!::scheduler.isInitialized || scheduler.isShutdown) {
            setup()
        }

        tracer.info("Starting task scheduler.")
        scheduler.listenerManager.addJobListener(TaskListener())
        scheduler.listenerManager.addTriggerListener(TaskTriggerListener())
        scheduler.start()
        tracer.info("Task scheduler started.")
    }

    /**
     * Stops the task scheduler.
     *
     * @param interrupt Whether the scheduler should interrupt all actively executing tasks.
     */
    fun stop(interrupt: Boolean) {
        tracer.info("Shutting down task scheduler.")
        if (::scheduler.isInitialized) {
            scheduler.shutdown(!interrupt)
            tracer.info("Task scheduler shut down.")
        } else {
            tracer.warning("SKipping Scheduler Shutdown. Task scheduler is not initialized.")
        }
    }

    /**
     * Restarts the task scheduler.
     *
     * @param interrupt Whether the scheduler should interrupt all actively executing tasks.
     * @return The current state of the task scheduler.
     */
    fun restart(interrupt: Boolean): TaskSchedulerState {
        tracer.info("Restarting task scheduler.")

        stop(interrupt = interrupt)
        start()

        return state().also {
            when (it) {
                TaskSchedulerState.RUNNING -> tracer.info("Task scheduler restarted.")
                else -> tracer.error("Task scheduler failed to restart.")
            }
        }
    }

    /**
     * Returns the current state of the task scheduler.
     */
    fun state(): TaskSchedulerState {
        return when {
            !::scheduler.isInitialized || !scheduler.isStarted -> TaskSchedulerState.STOPPED
            isPaused() -> TaskSchedulerState.PAUSED
            else -> TaskSchedulerState.RUNNING
        }
    }

    /**
     * Returns whether the task scheduler is started.
     */
    fun isStarted(): Boolean {
        return ::scheduler.isInitialized && scheduler.isStarted
    }

    /**
     * Determines if the task scheduler is currently paused.
     *
     * @return true if the scheduler has any paused trigger groups, false otherwise
     *
     * @see [pause]
     */
    fun isPaused(): Boolean {
        return ::scheduler.isInitialized && scheduler.pausedTriggerGroups.isNotEmpty()
    }

    /**
     * Configures the task scheduler to shut down when the application is stopped.
     *
     * @param environment The [ApplicationEnvironment] in which application runs.
     */
    fun configure(environment: ApplicationEnvironment) {
        // Add a shutdown hook to stop the scheduler when the application is stopped.
        environment.monitor.subscribe(ApplicationStopping) {
            stop(interrupt = false)
        }
    }

    /**
     * Pauses the scheduler and all currently scheduled tasks.
     *
     * While the scheduler is paused, new tasks can still be scheduled.
     * These tasks will only execute after the scheduler is resumed.
     * It is also possible to resume individual tasks independently
     * of the overall scheduler state. Thus, individual tasks can be
     * actively executing even when the scheduler is paused.
     *
     * @return [TaskStateChangeEntity] containing details of the operation.
     */
    fun pause(): TaskStateChangeEntity {
        return TaskState.change(scheduler = scheduler, targetState = TriggerState.PAUSED) {
            // Attempt to pause all triggers
            tracer.info("Attempting to pause all triggers...")
            scheduler.pauseAll()

            // Check if there are any non-paused trigger groups after pauseAll.
            val nonPausedGroups: List<String> = scheduler.jobGroupNames.filter { group ->
                scheduler.getJobKeys(GroupMatcher.jobGroupEquals(group)).any { jobKey ->
                    scheduler.getTriggersOfJob(jobKey).any { trigger ->
                        scheduler.getTriggerState(trigger.key) != TriggerState.PAUSED
                    }
                }
            }
            tracer.info("Non-paused trigger groups after pauseAll: $nonPausedGroups")

            // If there are non-paused groups, attempt to pause them individually.
            if (nonPausedGroups.isNotEmpty()) {
                nonPausedGroups.forEach { group ->
                    tracer.info("Pausing trigger group: $group")
                    scheduler.getJobKeys(GroupMatcher.jobGroupEquals(group)).forEach { jobKey ->
                        scheduler.getTriggersOfJob(jobKey).forEach { trigger ->
                            scheduler.pauseTrigger(trigger.key)
                        }
                    }
                }
            }

            // Verify if all triggers have been paused.
            val remainingNonPausedGroups: List<String> = scheduler.jobGroupNames.filter { group ->
                scheduler.getJobKeys(GroupMatcher.jobGroupEquals(group)).any { jobKey ->
                    scheduler.getTriggersOfJob(jobKey).any { trigger ->
                        scheduler.getTriggerState(trigger.key) != TriggerState.PAUSED
                    }
                }
            }
            if (remainingNonPausedGroups.isEmpty()) {
                tracer.info("All triggers have been paused successfully.")
            } else {
                tracer.error("The following trigger groups are still not paused: $remainingNonPausedGroups")
            }

            // Return the current state of the scheduler.
            state().name
        }
    }

    /**
     * Resumes all tasks currently paused in the scheduler.
     *
     * @return [TaskStateChangeEntity] containing details of the operation.
     */
    fun resume(): TaskStateChangeEntity {
        return TaskState.change(scheduler = scheduler, targetState = TriggerState.NORMAL) {
            // Attempt to resume all triggers.
            tracer.info("Attempting to resume all triggers.")
            scheduler.resumeAll()

            // Check if there are any paused trigger groups after resumeAll.
            val pausedGroups: MutableSet<String> = scheduler.pausedTriggerGroups
            tracer.info("Paused trigger groups after resumeAll: $pausedGroups")

            // If there are paused groups, attempt to resume them individually
            if (pausedGroups.isNotEmpty()) {
                pausedGroups.forEach { group ->
                    tracer.info("Resuming trigger group: $group")
                    scheduler.resumeTriggers(GroupMatcher.triggerGroupEquals(group))
                }
            }

            // Verify if all triggers have been resumed.
            val remainingPausedGroups: MutableSet<String> = scheduler.pausedTriggerGroups
            if (remainingPausedGroups.isEmpty()) {
                tracer.info("All triggers have been resumed successfully.")
            } else {
                tracer.error("The following trigger groups are still paused: $remainingPausedGroups")
            }

            // Return the current state of the scheduler.
            state().name
        }
    }

    /**
     * Returns the total number of tasks currently scheduled in the scheduler.
     */
    fun totalTasks(): Int {
        if (!::scheduler.isInitialized) {
            return 0
        }
        return tasks.all().size
    }

    /**
     * Helper object to manage tasks in the scheduler.
     */
    class Tasks private constructor(private val scheduler: Scheduler) {

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
         * @return [TaskStateChangeEntity] containing details of the operation.
         */
        fun pause(name: String, group: String): TaskStateChangeEntity {
            return TaskState.change(scheduler = scheduler, targetState = TriggerState.PAUSED) {
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
         * @return [TaskStateChangeEntity] containing details of the operation.
         */
        fun resume(name: String, group: String): TaskStateChangeEntity {
            return TaskState.change(scheduler = scheduler, targetState = TriggerState.NORMAL) {
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
         * Returns a snapshot list of all tasks currently scheduled in the scheduler.
         *
         * @param executing True if only actively executing tasks should be returned; false to return all tasks.
         * @return A list of [TaskScheduleEntity] objects representing the scheduled tasks.
         */
        fun all(executing: Boolean = false): List<TaskScheduleEntity> {
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
         * Helper method to create a [TaskScheduleEntity] from a [JobDetail] including the next fire time.
         *
         * @param taskDetail The task detail from which to create the [TaskScheduleEntity].
         * @return The constructed [TaskScheduleEntity].
         */
        private fun createTaskScheduleEntity(taskDetail: JobDetail): TaskScheduleEntity {
            val jobKey: JobKey = taskDetail.key
            val triggers: List<Trigger> = scheduler.getTriggersOfJob(jobKey)

            // Get the most restrictive state from the list of trigger states.
            val mostRestrictiveState: TriggerState = TaskState.getTriggerState(
                scheduler = scheduler,
                taskDetail = taskDetail
            )

            // Resolve the interval metrics.
            val (interval, runs) = triggers.firstOrNull()?.let { trigger ->
                if (trigger is SimpleTrigger) {
                    val repeatInterval: Duration = trigger.repeatInterval.toDuration(unit = DurationUnit.MILLISECONDS)
                    val totalSeconds: Long = repeatInterval.inWholeSeconds

                    if (totalSeconds != 0L) {
                        DateTimeUtils.formatDuration(duration = repeatInterval) to trigger.timesTriggered
                    } else null
                } else if (trigger is CronTrigger) {
                    trigger.cronExpression to null
                } else null
            } ?: (null to null)

            // Resolve the concrete parameters of the task.
            val dataMap: List<String> = taskDetail.jobDataMap
                .entries.map { (key, value) -> "$key: $value" }
                .sorted()

            // Determine the next fire time of the task.
            val nextFireTime: Date? = triggers.mapNotNull { it.nextFireTime }.minOrNull()

            return TaskScheduleEntity(
                name = jobKey.name,
                group = jobKey.group,
                consumer = taskDetail.jobClass.simpleName,
                nextFireTime = nextFireTime?.let { DateTimeUtils.javaDateToLocalDateTime(datetime = it) },
                state = mostRestrictiveState.name,
                interval = interval,
                runs = runs,
                dataMap = dataMap,
            )
        }

        companion object {
            internal fun create(scheduler: Scheduler): Tasks {
                return Tasks(scheduler = scheduler)
            }
        }
    }
}
