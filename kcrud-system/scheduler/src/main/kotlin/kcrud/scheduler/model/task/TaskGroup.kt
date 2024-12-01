/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.scheduler.model.task

import kcrud.core.plugins.Uuid
import kotlinx.serialization.Serializable

/**
 * Represents a task group.
 *
 * @property groupId The group of the task.
 * @property description The description of the task.
 */
@Serializable
public data class TaskGroup(
    val groupId: Uuid,
    val description: String
)
