/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.scheduler.audit

import io.perracodex.exposed.pagination.Page
import io.perracodex.exposed.pagination.Pageable
import kcrud.core.env.Tracer
import kcrud.scheduler.model.audit.AuditLog
import kcrud.scheduler.model.audit.AuditLogRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.uuid.Uuid

/**
 * Service to manage the persistence and retrieval of the scheduler audit logs.
 */
internal object AuditService {
    private val tracer = Tracer<AuditService>()

    /**
     * Creates a new audit entry.
     *
     * @param request The [AuditLogRequest] to create.
     */
    suspend fun create(request: AuditLogRequest): Uuid = withContext(Dispatchers.IO) {
        tracer.debug("Creating a new audit entry for task '${request.taskId}' in group '${request.groupId}'.")
        return@withContext AuditRepository.create(request = request)
    }

    /**
     * Finds all the audit entries, ordered bby the most recent first.
     *
     * @param pageable Optional pagination information.
     * @return The list of [AuditLog] instances.
     */
    suspend fun findAll(pageable: Pageable?): Page<AuditLog> = withContext(Dispatchers.IO) {
        tracer.debug("Finding all audit entries.")
        return@withContext AuditRepository.findAll(pageable = pageable)
    }

    /**
     * Finds all the audit logs for a concrete task by name and group, ordered by the most recent first.
     *
     * At least one of the [groupId] and [taskId] parameters should  be provided.
     *
     * @param pageable Optional pagination information.
     * @param groupId Optional group of the task.
     * @param taskId Optional unique identifier of the task.
     * @return The list of [AuditLog] instances, or an empty list if none found.
     */
    suspend fun find(pageable: Pageable?, groupId: Uuid?, taskId: String?): Page<AuditLog> = withContext(Dispatchers.IO) {
        tracer.debug("Finding audit entries for task '$taskId' in group '$groupId'.")
        return@withContext AuditRepository.find(pageable = pageable, groupId = groupId, taskId = taskId)
    }

    /**
     * Finds the most recent audit log for a specific task.
     *
     * @param groupId The group of the task.
     * @param taskId The unique identifier of the task.
     * @return The most recent [AuditLog] instance, or `null` if none found.
     */
    suspend fun mostRecent(groupId: Uuid, taskId: String): AuditLog? = withContext(Dispatchers.IO) {
        tracer.debug("Finding the most recent audit entry for task '$taskId' in group '$groupId'.")
        return@withContext AuditRepository.mostRecent(groupId = groupId, taskId = taskId)
    }

    /**
     * Returns the total count of audit entries for a specific task.
     *
     * @param groupId The group of the task.
     * @param taskId The unique identifier of the task.
     * @return The total count of audit entries for the task.
     */
    suspend fun count(groupId: Uuid, taskId: String): Int = withContext(Dispatchers.IO) {
        tracer.debug("Counting the audit entries for task '$taskId' in group '$groupId'.")
        return@withContext AuditRepository.count(groupId = groupId, taskId = taskId)
    }

    /**
     * Returns the total count of execution failures for a specific task.
     *
     * @param groupId The group of the task.
     * @param taskId The unique identifier of the task.
     * @return The total count of audit entries for the task.
     */
    suspend fun failures(groupId: Uuid, taskId: String): Int = withContext(Dispatchers.IO) {
        tracer.debug("Counting the failures for task '$taskId' in group '$groupId'.")
        return@withContext AuditRepository.failures(groupId = groupId, taskId = taskId)
    }
}
