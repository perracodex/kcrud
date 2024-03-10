/*
 * Copyright (c) 2023-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.graphql.expedia.types

import graphql.GraphQLContext
import graphql.execution.CoercedVariables
import graphql.language.StringValue
import graphql.language.Value
import graphql.schema.*
import kcrud.base.infrastructure.utils.KLocalDateTime
import java.util.*

/**
 * Generate custom GraphQL for the Kotlinx LocalDate type.
 */
val GraphLocalDateTimeType: GraphQLScalarType = GraphQLScalarType.newScalar()
    .name("LocalDateTime")
    .description("Type representing a LocalDateTime.")
    .coercing(LocalDateTimeCoercing)
    .build()

object LocalDateTimeCoercing : Coercing<KLocalDateTime, String> {
    override fun serialize(
        dataFetcherResult: Any,
        graphQLContext: GraphQLContext,
        locale: Locale
    ): String = runCatching {
        if (dataFetcherResult is KLocalDateTime) {
            dataFetcherResult.toString()
        } else {
            throw CoercingSerializeException("Data fetcher result $dataFetcherResult is not a valid LocalDateTime.")
        }
    }.getOrElse {
        throw CoercingSerializeException("Data fetcher result $dataFetcherResult cannot be serialized to a String.")
    }

    override fun parseValue(
        input: Any,
        graphQLContext: GraphQLContext,
        locale: Locale
    ): KLocalDateTime = runCatching {
        KLocalDateTime.parse(serialize(dataFetcherResult = input, graphQLContext = graphQLContext, locale = locale))
    }.getOrElse {
        throw CoercingParseValueException("Expected valid LocalDateTime but was $input.")
    }

    override fun parseLiteral(
        input: Value<*>,
        variables: CoercedVariables,
        graphQLContext: GraphQLContext,
        locale: Locale
    ): KLocalDateTime {
        val dateString = (input as? StringValue)?.value
        return runCatching {
            KLocalDateTime.parse(dateString!!)
        }.getOrElse {
            throw CoercingParseLiteralException("Expected valid LocalDateTime literal but was $dateString.")
        }
    }
}