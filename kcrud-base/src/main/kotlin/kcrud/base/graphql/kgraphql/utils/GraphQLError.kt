/*
 * Copyright (c) 2023-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.graphql.kgraphql.utils

import com.apurebase.kgraphql.GraphQLError
import kcrud.base.infrastructure.errors.BaseError
import kcrud.base.infrastructure.errors.KcrudException

object GraphQLError {
    fun of(error: BaseError): Nothing {
        val exception = KcrudException(error = error)
        throw GraphQLError(
            message = exception.messageDetail()
        )
    }
}
