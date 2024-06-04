/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.scheduler.service

import kcrud.base.settings.AppSettings
import org.quartz.Job
import org.quartz.JobDataMap
import org.quartz.JobExecutionContext

/**
 * Abstract class representing a task that can be executed by the scheduler.
 */
abstract class SchedulerTask : Job {

    override fun execute(context: JobExecutionContext?) {
        val jobDataMap: JobDataMap? = context?.mergedJobDataMap

        // Deserialize and reload AppSettings from the context to ensure this task
        // uses the current configuration, since it runs in a separate classloader.
        // This step is crucial as each classloader has its isolated instance of AppSettings.
        val appSettings: String? = jobDataMap?.get(SchedulerService.APP_SETTINGS_KEY) as String?
        appSettings?.let {
            AppSettings.deserialize(jsonString = it)
        }

        // Convert the bundled data into a map of properties.
        val properties: Map<String, Any> = jobDataMap?.toMap()
            ?.filterKeys { it is String }
            ?.mapKeys { it.key as String }
            ?: emptyMap()

        start(properties = properties)
    }

    /**
     * Triggered when the task is ready to be executed.
     *
     * @param properties The property bundle containing the task's parameters.
     */
    abstract fun start(properties: Map<String, Any>)
}