/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.scheduler.listeners

import kcrud.base.infrastructure.utils.Tracer
import kcrud.base.scheduler.annotation.JobSchedulerAPI
import org.quartz.JobExecutionContext
import org.quartz.Trigger
import org.quartz.Trigger.CompletedExecutionInstruction
import org.quartz.TriggerListener

@JobSchedulerAPI
class KcrudTriggerListener : TriggerListener {
    private val tracer = Tracer<KcrudTriggerListener>()

    override fun getName() = KcrudTriggerListener::class.simpleName

    override fun triggerFired(trigger: Trigger, context: JobExecutionContext) {
        tracer.debug("Job scheduler trigger fired: ${trigger.key}")
    }

    override fun triggerComplete(
        trigger: Trigger,
        context: JobExecutionContext,
        triggerInstructionCode: CompletedExecutionInstruction
    ) {
        tracer.debug("Job scheduler trigger completed: ${trigger.key}")
    }

    override fun triggerMisfired(trigger: Trigger) {
        tracer.debug("Job scheduler trigger misfired: ${trigger.key}")
    }

    override fun vetoJobExecution(trigger: Trigger, context: JobExecutionContext) = false
}
