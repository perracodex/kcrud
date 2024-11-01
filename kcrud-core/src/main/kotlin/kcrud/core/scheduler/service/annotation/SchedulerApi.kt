/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.core.scheduler.service.annotation

/**
 * Annotation for controlled access to the Scheduler API.
 */
@RequiresOptIn(level = RequiresOptIn.Level.ERROR, message = "Only to be used within the Scheduler API.")
@Retention(AnnotationRetention.BINARY)
internal annotation class SchedulerApi
