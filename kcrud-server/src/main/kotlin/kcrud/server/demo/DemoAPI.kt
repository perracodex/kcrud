/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.server.demo

/**
 * Annotation for controlled access to the Demo API.
 */
@RequiresOptIn(level = RequiresOptIn.Level.ERROR, message = "Only to be used within the Demo API.")
@Retention(AnnotationRetention.BINARY)
annotation class DemoAPI
