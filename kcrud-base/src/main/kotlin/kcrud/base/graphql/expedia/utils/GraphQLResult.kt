/*
 * Copyright (c) 2023-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.graphql.expedia.utils

import graphql.GraphqlErrorException
import graphql.execution.DataFetcherResult
import kcrud.base.infrastructure.errors.BaseError
import kcrud.base.infrastructure.errors.KcrudException

object GraphQLResult {
    fun <T : Any?> of(data: T, error: BaseError?): DataFetcherResult<T> {

        val result: DataFetcherResult.Builder<T> = DataFetcherResult.newResult<T>()
            .data(data)

        error?.let {
            val graphQLError: GraphqlErrorException = GraphqlErrorException.newErrorException()
                .cause(KcrudException(error = it))
                .extensions(mapOf("code" to it.code, "status" to it.status.value))
                .message(it.description)
                .build()

            result.error(graphQLError)
        }

        return result.build()
    }
}
