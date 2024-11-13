/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.core.scheduler.service.task

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
    override fun execute(context: JobExecutionContext) {
        val jobDataMap: JobDataMap? = context.mergedJobDataMap

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