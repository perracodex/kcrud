/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.scheduler.annotation

/**
 * Annotation for controlled access to the Scheduler API.
 */
@RequiresOptIn(level = RequiresOptIn.Level.ERROR, message = "Only to be used within the Scheduler API.")
@Retention(AnnotationRetention.BINARY)
annotation class SchedulerAPI
