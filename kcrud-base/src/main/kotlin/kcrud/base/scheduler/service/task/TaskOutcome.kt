/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.scheduler.service.task

/**
 * Represents a scheduler task outcome status.
 */
enum class TaskOutcome {
    /** The task has completed successfully. */
    SUCCESS,

    /** The task has failed to complete. */
    ERROR,
}
