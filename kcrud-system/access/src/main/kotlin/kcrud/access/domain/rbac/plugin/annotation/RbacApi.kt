/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.access.domain.rbac.plugin.annotation

/**
 * Annotation for controlled access to the RBAC API.
 */
@RequiresOptIn(level = RequiresOptIn.Level.ERROR, message = "Only to be used within the RBAC API.")
@Retention(AnnotationRetention.BINARY)
internal annotation class RbacApi
