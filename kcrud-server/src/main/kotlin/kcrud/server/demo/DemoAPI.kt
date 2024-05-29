/*
 * Copyright (c) 2024-Present Perracodex. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.server.demo

/**
 * Annotation for controlled access to the Demo API.
 */
@RequiresOptIn(level = RequiresOptIn.Level.ERROR, message = "Only to be used within the Demo API.")
@Retention(AnnotationRetention.BINARY)
annotation class DemoAPI
