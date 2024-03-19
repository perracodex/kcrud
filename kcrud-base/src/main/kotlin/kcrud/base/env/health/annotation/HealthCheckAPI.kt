/*
 * Copyright (c) 2024-Present Perraco. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.env.health.annotation

/**
 * Annotation for controlled access to the Health Check API.
 */
@RequiresOptIn(level = RequiresOptIn.Level.ERROR, message = "Only to be used within the Health Check API.")
@Retention(AnnotationRetention.BINARY)
annotation class HealthCheckAPI
