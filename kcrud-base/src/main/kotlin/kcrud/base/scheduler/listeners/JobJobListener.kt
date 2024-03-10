/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.scheduler.listeners

import kcrud.base.infrastructure.utils.Tracer
import kcrud.base.scheduler.annotation.JobSchedulerAPI
import kcrud.base.security.service.AuthenticationTokenService
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.quartz.JobListener

@JobSchedulerAPI
class KcrudJobListener : JobListener {
    private val tracer = Tracer<AuthenticationTokenService>()

    override fun getName() = KcrudJobListener::class.simpleName

    override fun jobToBeExecuted(context: JobExecutionContext) {
        tracer.debug("Job to be executed: ${context.jobDetail.key}")
    }

    override fun jobExecutionVetoed(context: JobExecutionContext) {
        tracer.debug("Job execution vetoed: ${context.jobDetail.key}")
    }

    override fun jobWasExecuted(context: JobExecutionContext, jobException: JobExecutionException?) {
        tracer.debug("Job executed: ${context.jobDetail.key}")
    }
}
