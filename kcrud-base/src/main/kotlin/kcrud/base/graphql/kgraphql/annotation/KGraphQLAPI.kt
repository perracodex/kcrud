/*
 * Copyright (c) 2023-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.graphql.kgraphql.annotation

/**
 * Annotation for controlled access to the KGraphQL API.
 */
@RequiresOptIn(level = RequiresOptIn.Level.ERROR, message = "Only to be used within the KGraphQL API.")
@Retention(AnnotationRetention.BINARY)
annotation class KGraphQLAPI
