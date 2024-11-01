/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.core.scheduler.service.task

import kcrud.core.scheduler.service.SchedulerService
import kcrud.core.scheduler.service.annotation.SchedulerApi
import kcrud.core.settings.AppSettings
import org.quartz.Job
import org.quartz.Scheduler
import org.quartz.spi.JobFactory
import org.quartz.spi.TriggerFiredBundle
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

/**
 * Custom [JobFactory] implementation that create new task instances.
 * Can be used to inject dependencies into the task instances or bundle additional data.
 */
@SchedulerApi
internal class TaskFactory : JobFactory {

    override fun newJob(bundle: TriggerFiredBundle, scheduler: Scheduler): Job {
        // Add the AppSettings into the data map so that the task instance can access it.
        // This step is crucial as each classloader has its isolated instance of AppSettings.
        bundle.jobDetail.jobDataMap[SchedulerService.APP_SETTINGS_KEY] = AppSettings.serialize()
        val jobClass: KClass<out Job> = bundle.jobDetail.jobClass.kotlin
        return jobClass.createInstance()
    }
}
