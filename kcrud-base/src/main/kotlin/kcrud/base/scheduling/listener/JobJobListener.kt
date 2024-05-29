/*
 * Copyright (c) 2024-Present Perracodex. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.scheduling.listener

import kcrud.base.env.Tracer
import kcrud.base.scheduling.annotation.JobSchedulerAPI
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.quartz.JobListener

/**
 * A listener for scheduler jobs that logs job execution events.
 */
@JobSchedulerAPI
class KcrudJobListener : JobListener {
    private val tracer = Tracer<KcrudJobListener>()

    /**
     * The name of the job listener.
     */
    override fun getName(): String? = KcrudJobListener::class.simpleName

    /**
     * Called by the [org.quartz.Scheduler] when a [org.quartz.JobDetail]
     * is about to be executed (an associated [org.quartz.Trigger] has occurred).
     *
     * This method will not be invoked if the execution of the Job was vetoed
     * by a [org.quartz.TriggerListener].
     *
     * @see jobExecutionVetoed
     */
    override fun jobToBeExecuted(context: JobExecutionContext) {
        tracer.debug("Job to be executed: ${context.jobDetail.key}")
    }

    /**
     * Called by the [org.quartz.Scheduler] when a [org.quartz.JobDetail]
     * was about to be executed (an associated [org.quartz.Trigger] has occurred),
     * but a [org.quartz.TriggerListener] vetoed its execution.
     *
     * @see [jobToBeExecuted]
     */
    override fun jobExecutionVetoed(context: JobExecutionContext) {
        tracer.debug("Job execution vetoed: ${context.jobDetail.key}")
    }

    /**
     * Called by the [org.quartz.Scheduler] after a [org.quartz.JobDetail]
     * has been executed, and be for the associated [org.quartz.Trigger]'s
     * triggered(xx) method has been called.
     */
    override fun jobWasExecuted(context: JobExecutionContext, jobException: JobExecutionException?) {
        tracer.debug("Job executed: ${context.jobDetail.key}")
    }
}
