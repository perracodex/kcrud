/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.domain.employee.routing.annotation

/**
 * Annotation for controlled access to the Employee Routes API.
 */
@RequiresOptIn(level = RequiresOptIn.Level.ERROR, message = "Only to be used within the Employee Routes API.")
@Retention(AnnotationRetention.BINARY)
annotation class EmployeeRouteAPI
