/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.access.token.annotation

/**
 * Annotation for controlled access to the Token API.
 */
@RequiresOptIn(level = RequiresOptIn.Level.ERROR, message = "Only to be used within the Token API.")
@Retention(AnnotationRetention.BINARY)
internal annotation class TokenAPI
