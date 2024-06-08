/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.scheduler.listener

import kcrud.base.env.Tracer
import kcrud.base.scheduler.annotation.SchedulerAPI
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.quartz.JobListener

/**
 * A listener for scheduler that logs task execution events.
 */
@SchedulerAPI
class TaskListener : JobListener {
    private val tracer = Tracer<TaskListener>()

    /**
     * The name of the task listener.
     */
    override fun getName(): String? = TaskListener::class.simpleName

    /**
     * Called by the [org.quartz.Scheduler] when a [org.quartz.JobDetail]
     * is about to be executed (an associated [org.quartz.Trigger] has occurred).
     *
     * This method will not be invoked if the execution of the Job was vetoed
     * by a [org.quartz.TriggerListener].
     *
     * @see [jobExecutionVetoed]
     */
    override fun jobToBeExecuted(context: JobExecutionContext) {
        tracer.debug("Task to be executed: ${context.jobDetail.key}")
    }

    /**
     * Called by the [org.quartz.Scheduler] when a [org.quartz.JobDetail]
     * was about to be executed (an associated [org.quartz.Trigger] has occurred),
     * but a [org.quartz.TriggerListener] vetoed its execution.
     *
     * @see jobToBeExecuted
     */
    override fun jobExecutionVetoed(context: JobExecutionContext) {
        tracer.debug("Task execution vetoed: ${context.jobDetail.key}")
    }

    /**
     * Called by the [org.quartz.Scheduler] after a [org.quartz.JobDetail]
     * has been executed, and be for the associated [org.quartz.Trigger]'s
     * triggered(xx) method has been called.
     */
    override fun jobWasExecuted(context: JobExecutionContext, jobException: JobExecutionException?) {
        tracer.debug("Task executed: ${context.jobDetail.key}")
    }
}
