/*
 * Copyright (c) 2024-Present Perracodex. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.scheduling.service

import kcrud.base.settings.AppSettings
import org.quartz.Job
import org.quartz.Scheduler
import org.quartz.spi.JobFactory
import org.quartz.spi.TriggerFiredBundle
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

/**
 * Custom [JobFactory] implementation that create new job instances.
 * Can be used to inject dependencies into the job instances or bundle additional data.
 */
internal class CustomJobFactory : JobFactory {
    /**
     * Creates a new job instance.
     */
    override fun newJob(bundle: TriggerFiredBundle, scheduler: Scheduler): Job {
        // Add the AppSettings into the job data map so that the job instance can access it.
        // This step is crucial as each classloader has its isolated instance of AppSettings.
        bundle.jobDetail.jobDataMap[JobSchedulerService.APP_SETTINGS_KEY] = AppSettings.serialize()
        val jobClass: KClass<out Job> = bundle.jobDetail.jobClass.kotlin
        return jobClass.createInstance()
    }
}
