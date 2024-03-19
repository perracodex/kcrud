/*
 * Copyright (c) 2023-Present Perraco. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.access.rbac.plugin.annotation

/**
 * Annotation for controlled access to the RBAC API.
 */
@RequiresOptIn(level = RequiresOptIn.Level.ERROR, message = "Only to be used within the RBAC API.")
@Retention(AnnotationRetention.BINARY)
annotation class RbacAPI
