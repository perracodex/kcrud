/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.scheduler.service.task

import kcrud.base.scheduler.service.core.SchedulerService
import kcrud.base.settings.AppSettings
import org.quartz.Job
import org.quartz.JobDataMap
import org.quartz.JobExecutionContext

/**
 * Abstract class representing a scheduled task.
 * This class provides the mechanism to consume task parameters
 * and execute the task.
 *
 * Subclasses must implement the [start] method to define the task's behavior.
 */
public abstract class TaskConsumer : Job {

    /**
     * Initiates the task execution.
     *
     * @param context The job execution context.
     */
    override fun execute(context: JobExecutionContext?) {
        val jobDataMap: JobDataMap? = context?.mergedJobDataMap

        // Deserialize and reload AppSettings from the context to ensure this task
        // uses the current configuration, since it runs in a separate classloader.
        // This step is crucial as each classloader has its isolated instance of AppSettings.
        val appSettings: String? = jobDataMap?.get(SchedulerService.APP_SETTINGS_KEY) as String?
        appSettings?.let {
            AppSettings.deserialize(string = it)
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
    public abstract fun start(properties: Map<String, Any>)
}