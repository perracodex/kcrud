/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.scheduler.service.task

import kcrud.base.scheduler.annotation.SchedulerAPI
import kcrud.base.scheduler.entity.TaskStateChangeEntity
import org.quartz.JobDetail
import org.quartz.JobKey
import org.quartz.Scheduler
import org.quartz.Trigger
import org.quartz.Trigger.TriggerState
import org.quartz.impl.matchers.GroupMatcher

/**
 * Utility class for managing the task state in the scheduler.
 */
@SchedulerAPI
object TaskState {

    /**
     * Changes the state of all tasks (either pausing or resuming them) and returns detailed results of the change.
     *
     * @param scheduler The scheduler instance to use for the state change.
     * @param targetState The expected state of tasks after the operation.
     * @param action The lambda function that executes the state change.
     * @return [TaskStateChangeEntity] detailing the affected task counts.
     */
    fun change(scheduler: Scheduler, targetState: TriggerState, action: () -> String): TaskStateChangeEntity {
        // Retrieve the states of all tasks before and after performing the state change action.
        val beforeStates: Map<JobKey, TriggerState> = getAllStates(scheduler = scheduler)
        val schedulerState: String = action()
        val afterStates: Map<JobKey, TriggerState> = getAllStates(scheduler = scheduler)

        // Count the total number of tasks that were affected by the action.
        // A task is considered affected if it was not already in the target
        // state and has changed to the target state.
        val totalAffected: Int = afterStates.count { (key, state) ->
            state == targetState && beforeStates[key]?.let { it != state } ?: true
        }

        // Count the total number of tasks that remained in the target state both
        // before and after the action. This includes tasks that were already in
        // the target state and were unaffected by the action.
        val alreadyInState: Int = afterStates.count { (key, state) ->
            state == targetState && beforeStates[key]?.let { it == state } ?: false
        }

        return TaskStateChangeEntity(
            totalAffected = totalAffected,
            alreadyInState = alreadyInState,
            totalTasks = afterStates.size,
            state = schedulerState
        )
    }

    /**
     * Retrieves the most restrictive state of a task by examining all associated triggers.
     *
     * @param scheduler The scheduler instance to use for the state retrieval.
     * @param jobKey The job key of the task to retrieve the state for.
     * @return The most restrictive trigger state of the task.
     */
    fun getTriggerState(scheduler: Scheduler, jobKey: JobKey): TriggerState {
        return getMostRestrictiveTriggerState(scheduler, jobKey)
    }

    /**
     * Retrieves the most restrictive state of a task by examining all associated triggers.
     *
     * @param scheduler The scheduler instance to use for the state retrieval.
     * @param taskDetail The job detail of the task to retrieve the state for.
     * @return The most restrictive trigger state of the task.
     */
    fun getTriggerState(scheduler: Scheduler, taskDetail: JobDetail): TriggerState {
        return getMostRestrictiveTriggerState(scheduler, taskDetail.key)
    }

    /**
     * Retrieves the most restrictive state of a task by examining all associated triggers.
     *
     * @param scheduler The scheduler instance to use for the state retrieval.
     * @param jobKey The job key of the task to retrieve the state for.
     * @return The most restrictive trigger state of the task.
     */
    private fun getMostRestrictiveTriggerState(scheduler: Scheduler, jobKey: JobKey): TriggerState {
        val triggers: List<Trigger> = scheduler.getTriggersOfJob(jobKey)
        if (triggers.isEmpty()) {
            return TriggerState.NONE
        }

        val triggerStates: List<TriggerState> = triggers.map { scheduler.getTriggerState(it.key) }
        return when {
            triggerStates.any { it == TriggerState.PAUSED } -> TriggerState.PAUSED
            triggerStates.any { it == TriggerState.BLOCKED } -> TriggerState.BLOCKED
            triggerStates.any { it == TriggerState.ERROR } -> TriggerState.ERROR
            triggerStates.any { it == TriggerState.COMPLETE } -> TriggerState.COMPLETE
            else -> TriggerState.NORMAL  // Assuming NORMAL as default if no other states are found.
        }
    }

    /**
     * Retrieves the state of all tasks currently scheduled in the scheduler,
     * by compiling a map where each task key is associated with its most
     * restrictive (or most significant) trigger state.
     *
     * @param scheduler The scheduler instance to use for the state retrieval.
     *
     * @return A map of JobKeys to their respective TriggerStates.
     */
    private fun getAllStates(scheduler: Scheduler): Map<JobKey, TriggerState> {
        // 1. Fetch all task keys across all task groups within the scheduler.
        // 2. For each task key, retrieve all associated triggers.
        // 3. For each trigger, obtain its current state.
        // 4. From the list of trigger states, find the one with the highest priority (lowest ordinal value).
        // 5. If no triggers are found, or all are null, default to TriggerState.NONE.
        return scheduler.getJobKeys(GroupMatcher.anyGroup()).associateWith { jobKey ->
            scheduler.getTriggersOfJob(jobKey).mapNotNull { trigger ->
                scheduler.getTriggerState(trigger.key)
            }.minByOrNull { it.ordinal } ?: TriggerState.NONE
        }
    }
}
