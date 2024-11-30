/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.scheduler.service

import io.ktor.server.application.*
import kcrud.core.env.Tracer
import kcrud.core.event.AsyncScope
import kcrud.scheduler.listener.TaskListener
import kcrud.scheduler.listener.TriggerListener
import kcrud.scheduler.model.task.TaskStateChange
import kcrud.scheduler.service.SchedulerService.pause
import kcrud.scheduler.task.TaskFactory
import kcrud.scheduler.task.TaskState
import org.quartz.Scheduler
import org.quartz.SchedulerFactory
import org.quartz.Trigger.TriggerState
import org.quartz.impl.StdSchedulerFactory
import org.quartz.impl.matchers.GroupMatcher
import java.util.*

/**
 * The task scheduler service managing the scheduling and execution of tasks.
 *
 * #### References
 * - [Quartz Scheduler Documentation](https://github.com/quartz-scheduler/quartz/blob/main/docs/index.adoc)
 * - [Quartz Scheduler Configuration](https://www.quartz-scheduler.org/documentation/2.4.0-SNAPSHOT/configuration.html)
 */
internal object SchedulerService {
    private val tracer: Tracer = Tracer<SchedulerService>()

    /** The possible states of the task scheduler. */
    internal enum class TaskSchedulerState {
        /** The scheduler is running. */
        RUNNING,

        /** The scheduler is paused. */
        PAUSED,

        /** The scheduler is stopped. */
        STOPPED
    }

    /** Scheduler instance used to manage tasks. */
    private lateinit var scheduler: Scheduler

    /** Manage tasks in the scheduler. */
    internal lateinit var tasks: SchedulerTasks
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

        tasks = SchedulerTasks(scheduler = scheduler)

        tracer.debug("Task scheduler configured.")
    }

    /**
     * Starts the task scheduler.
     */
    fun start() {
        if (!SchedulerService::scheduler.isInitialized || scheduler.isShutdown) {
            setup()
        }

        tracer.info("Starting task scheduler.")
        Thread.currentThread().contextClassLoader = this::class.java.classLoader
        scheduler.listenerManager.addJobListener(TaskListener())
        scheduler.listenerManager.addTriggerListener(TriggerListener())
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
        if (SchedulerService::scheduler.isInitialized) {
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
            !SchedulerService::scheduler.isInitialized || !scheduler.isStarted -> TaskSchedulerState.STOPPED
            isPaused() -> TaskSchedulerState.PAUSED
            else -> TaskSchedulerState.RUNNING
        }
    }

    /**
     * Returns whether the task scheduler is started.
     */
    fun isStarted(): Boolean {
        return SchedulerService::scheduler.isInitialized && scheduler.isStarted
    }

    /**
     * Determines if the task scheduler is currently paused.
     *
     * @return true if the scheduler has any paused trigger groups, false otherwise
     *
     * @see [pause]
     */
    fun isPaused(): Boolean {
        return SchedulerService::scheduler.isInitialized && scheduler.pausedTriggerGroups.isNotEmpty()
    }

    /**
     * Configures the task scheduler to shut down when the application is stopped.
     *
     * @param application The server [Application] instance.
     */
    fun configure(application: Application) {
        // Add a shutdown hook to stop the scheduler when the application is stopped.
        application.monitor.subscribe(ApplicationStopping) {
            stop(interrupt = false)
            AsyncScope.close()
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
     * @return [TaskStateChange] containing details of the operation.
     */
    fun pause(): TaskStateChange {
        return TaskState.change(scheduler = scheduler, targetState = TriggerState.PAUSED) {
            // Attempt to pause all triggers
            tracer.info("Attempting to pause all triggers...")
            scheduler.pauseAll()

            // Find any non-paused trigger groups.
            val nonPausedGroups = findNonPausedGroups()
            if (nonPausedGroups.isNotEmpty()) {
                tracer.info("Non-paused trigger groups after pauseAll: $nonPausedGroups")

                // Attempt to pause remaining groups individually.
                nonPausedGroups.forEach { group ->
                    tracer.info("Pausing trigger group: $group")
                    scheduler.getJobKeys(GroupMatcher.jobGroupEquals(group)).forEach { jobKey ->
                        scheduler.getTriggersOfJob(jobKey).forEach { trigger ->
                            scheduler.pauseTrigger(trigger.key)
                        }
                    }
                }
            }

            // Final check to ensure all triggers are paused.
            val remainingNonPausedGroups = findNonPausedGroups()
            if (remainingNonPausedGroups.isEmpty()) {
                tracer.info("All triggers have been paused successfully.")
            } else {
                tracer.error("The following trigger groups are still not paused: $remainingNonPausedGroups")
            }

            // Return the current state of the scheduler.
            return@change state().name
        }
    }

    /**
     * Finds groups with non-paused triggers in the scheduler.
     *
     * @return A list of group names with non-paused triggers.
     */
    private fun findNonPausedGroups(): List<String> {
        return scheduler.jobGroupNames.filter { group ->
            scheduler.getJobKeys(GroupMatcher.jobGroupEquals(group)).any { jobKey ->
                scheduler.getTriggersOfJob(jobKey).any { trigger ->
                    scheduler.getTriggerState(trigger.key) != TriggerState.PAUSED
                }
            }
        }
    }

    /**
     * Resumes all tasks currently paused in the scheduler.
     *
     * @return [TaskStateChange] containing details of the operation.
     */
    fun resume(): TaskStateChange {
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
            return@change state().name
        }
    }

    /**
     * Returns the total number of tasks currently scheduled in the scheduler.
     */
    suspend fun totalTasks(): Int {
        if (!SchedulerService::scheduler.isInitialized) {
            return 0
        }
        return tasks.all().size
    }
}
