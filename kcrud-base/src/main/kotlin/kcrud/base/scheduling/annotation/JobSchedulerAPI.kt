/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.scheduling.annotation

/**
 * Annotation for controlled access to the Job Scheduler API.
 */
@RequiresOptIn(level = RequiresOptIn.Level.ERROR, message = "Only to be used within the Job Scheduler API.")
@Retention(AnnotationRetention.BINARY)
annotation class JobSchedulerAPI
