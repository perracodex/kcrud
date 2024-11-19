/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.scheduler.service.task

import kcrud.core.event.SseService
import kcrud.core.util.DateTimeUtils.current
import kcrud.core.util.DateTimeUtils.formatted
import kcrud.scheduler.service.task.TaskConsumer.Payload
import kotlinx.datetime.LocalDateTime
import org.quartz.Job
import org.quartz.JobDataMap
import org.quartz.JobExecutionContext
import kotlin.uuid.Uuid

/**
 * Abstract class representing a scheduled task.
 * This class provides the mechanism to consume task parameters
 * and execute the task.
 *
 * Subclasses must implement the [start] method to define the task's behavior.
 */
public abstract class TaskConsumer<P : Payload> : Job {

    /**
     * Initiates the task execution.
     *
     * @param context The job execution context.
     */
    override fun execute(context: JobExecutionContext) {
        val jobDataMap: JobDataMap = context.mergedJobDataMap

        // Convert the bundled data into a map of properties.
        val properties: Map<String, Any> = jobDataMap.toMap()
            .filterKeys { it is String }
            .mapKeys { it.key as String }

        start(properties = properties)
    }

    /**
     * Starts the task with standardized error handling.
     *
     * @param properties The property bundle containing the task's parameters.
     */
    private fun start(properties: Map<String, Any>) {
        val payload: P = buildPayload(properties = properties)

        runCatching {
            consume(payload = payload)
        }.onFailure { error ->
            SseService.push(
                message = "${LocalDateTime.current().formatted(timeDelimiter = " | ", precision = 6)} " +
                        "| Failed to consume task type '${payload.taskType}' " +
                        "| Group Id: ${payload.groupId} " +
                        "| Task Id: ${payload.taskId} " +
                        "| Error: ${error.message.orEmpty()}"
            )
            // Rethrow the exception to allow it to propagate.
            throw error
        }.onSuccess {
            SseService.push(
                message = "${LocalDateTime.current().formatted(timeDelimiter = " | ", precision = 6)} " +
                        "| Consumed task type '${payload.taskType}' " +
                        "| Group Id: ${payload.groupId} " +
                        "| Task Id: ${payload.taskId} "
            )
        }
    }

    /**
     * Builds the payload from properties.
     *
     * @param properties The property bundle.
     * @return The constructed payload.
     */
    protected abstract fun buildPayload(properties: Map<String, Any>): P

    /**
     * Processes the task with the provided payload.
     * Extending classes must implement this method to define
     * the task-specific consumption behavior.
     *
     * @param payload The payload containing the data required to process the task.
     */
    protected abstract fun consume(payload: P)

    /**
     * Common interface for task payloads.
     *
     * @property groupId The unique identifier of the task group.
     * @property taskId The unique identifier of the task.
     * @property taskType A string representing the type of task.
     */
    public interface Payload {
        public val groupId: Uuid
        public val taskId: String
        public val taskType: String
    }
}
