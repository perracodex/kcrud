/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.domain.employment.api.annotation

/**
 * Annotation for controlled access to the Employment Routes API.
 */
@RequiresOptIn(level = RequiresOptIn.Level.ERROR, message = "Only to be used within the Employment Routes API.")
@Retention(AnnotationRetention.BINARY)
internal annotation class EmploymentRouteAPI
