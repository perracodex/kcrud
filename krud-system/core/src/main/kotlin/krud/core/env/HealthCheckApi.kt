/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package krud.core.env

/**
 * Annotation for controlled access to the Health Check API.
 */
@RequiresOptIn(level = RequiresOptIn.Level.ERROR, message = "Only to be used within the Health Check API.")
@Retention(AnnotationRetention.BINARY)
public annotation class HealthCheckApi
