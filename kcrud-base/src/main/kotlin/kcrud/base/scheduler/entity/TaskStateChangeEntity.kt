/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.scheduler.entity

import kotlinx.serialization.Serializable

/**
 * Data class to store the result when changing the state of a scheduler task.
 *
 * @property totalAffected The total number of tasks affected by the operation.
 * @property alreadyInState The number of tasks that were already in the desired state.
 * @property totalTasks The total number of tasks in the system.
 */
@Serializable
data class TaskStateChangeEntity(
    val totalAffected: Int,
    val alreadyInState: Int,
    val totalTasks: Int
)
