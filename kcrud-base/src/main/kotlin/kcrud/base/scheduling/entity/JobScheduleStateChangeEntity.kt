/*
 * Copyright (c) 2024-Present Perracodex. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.scheduling.entity

import kotlinx.serialization.Serializable

/**
 * Data class to store the result of pause and resume operations.
 *
 * @property totalAffected The total number of jobs affected by the operation.
 * @property alreadyInState The number of jobs that were already in the desired state.
 * @property totalJobs The total number of jobs in the system.
 */
@Serializable
data class JobScheduleStateChangeEntity(
    val totalAffected: Int,
    val alreadyInState: Int,
    val totalJobs: Int
)
