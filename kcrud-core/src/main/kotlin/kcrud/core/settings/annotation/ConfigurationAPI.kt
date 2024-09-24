/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.core.settings.annotation

/**
 * Annotation for controlled access to the Configuration API.
 */
@RequiresOptIn(level = RequiresOptIn.Level.ERROR, message = "Only to be used within the Configuration API.")
@Retention(AnnotationRetention.BINARY)
internal annotation class ConfigurationAPI
