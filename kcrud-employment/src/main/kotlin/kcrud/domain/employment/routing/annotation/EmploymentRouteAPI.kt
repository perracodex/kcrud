/*
 * Copyright (c) 2023-Present Perraco. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.domain.employment.routing.annotation

/**
 * Annotation for controlled access to the Employment Routes API.
 */
@RequiresOptIn(level = RequiresOptIn.Level.ERROR, message = "Only to be used within the Employment Routes API.")
@Retention(AnnotationRetention.BINARY)
annotation class EmploymentRouteAPI
