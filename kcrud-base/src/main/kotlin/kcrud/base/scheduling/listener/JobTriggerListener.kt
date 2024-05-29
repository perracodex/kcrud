/*
 * Copyright (c) 2024-Present Perracodex. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.scheduling.listener

import kcrud.base.env.Tracer
import kcrud.base.scheduling.annotation.JobSchedulerAPI
import org.quartz.JobExecutionContext
import org.quartz.Trigger
import org.quartz.Trigger.CompletedExecutionInstruction
import org.quartz.TriggerListener

/**
 * The interface to be implemented by classes that want to be informed
 * when a job schedule trigger fires.
 */
@JobSchedulerAPI
class KcrudTriggerListener : TriggerListener {
    private val tracer = Tracer<KcrudTriggerListener>()

    /**
     * Get the name of the [TriggerListener].
     */
    override fun getName(): String? = KcrudTriggerListener::class.simpleName

    /**
     * Called by the [org.quartz.Scheduler] when a [Trigger] has fired,
     * and it's associated [org.quartz.JobDetail] is about to be executed.
     *
     * It is called before the [vetoJobExecution](...) method.
     *
     * @param trigger The [Trigger] that has fired.
     * @param context The [JobExecutionContext] that will be passed to the [org.quartz.Job]'s execute(xx) method.
     */
    override fun triggerFired(trigger: Trigger, context: JobExecutionContext) {
        tracer.debug("Job scheduler trigger fired: ${trigger.key}")
    }

    /**
     * Called by the [org.quartz.Scheduler] when a [Trigger] has fired,
     * it's associated [org.quartz.JobDetail] has been executed, and it's triggered(xx) method has been called.
     *
     * @param trigger The [Trigger] that was fired.
     * @param context The [JobExecutionContext] that was passed to the [org.quartz.Job]'s execute(xx) method.
     * @param triggerInstructionCode The result of the call on the [Trigger]'s triggered(xx) method.
     */
    override fun triggerComplete(
        trigger: Trigger,
        context: JobExecutionContext,
        triggerInstructionCode: CompletedExecutionInstruction
    ) {
        tracer.debug("Job scheduler trigger completed: ${trigger.key}")
    }

    /**
     * Called by the [org.quartz.Scheduler] when a [Trigger] has misfired.
     *
     * Consideration should be given to how much time is spent in this method,
     * as it will affect all triggers that are misfiring. If you have lots
     * of triggers misfiring at once, it could be an issue it this method does a lot.
     *
     * @param trigger The [Trigger] that has misfired.
     */
    override fun triggerMisfired(trigger: Trigger) {
        tracer.debug("Job scheduler trigger misfired: ${trigger.key}")
    }

    /**
     * Called by the [org.quartz.Scheduler] when a [Trigger] has fired,
     * and it's associated [org.quartz.JobDetail] is about to be executed.
     * If the implementation vetoes the execution (via returning true), the job's execute
     * method will not be called.
     *
     * It is called after the [triggerFired] method.
     *
     * @param trigger The [Trigger] that has fired.
     * @param context The [JobExecutionContext] that will be passed to [org.quartz.Job]'s execute(xx) method.
     */
    override fun vetoJobExecution(trigger: Trigger, context: JobExecutionContext): Boolean = false
}
