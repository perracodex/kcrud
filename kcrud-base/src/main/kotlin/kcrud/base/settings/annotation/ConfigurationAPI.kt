/*
 * Copyright (c) 2023-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.settings.annotation

/**
 * Annotation for controlled access to the Configuration API.
 */
@RequiresOptIn(level = RequiresOptIn.Level.ERROR, message = "Only to be used within the Configuration API.")
@Retention(AnnotationRetention.BINARY)
annotation class ConfigurationAPI
