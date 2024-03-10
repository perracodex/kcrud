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
import kcrud.base.infrastructure.utils.KLocalDate
import java.util.*

/**
 * Generate custom GraphQL for the Kotlinx LocalDate type.
 */
val GraphLocalDateType: GraphQLScalarType = GraphQLScalarType.newScalar()
    .name("LocalDate")
    .description("Type representing a LocalDate.")
    .coercing(LocalDateCoercing)
    .build()

object LocalDateCoercing : Coercing<KLocalDate, String> {
    override fun serialize(
        dataFetcherResult: Any,
        graphQLContext: GraphQLContext,
        locale: Locale
    ): String = runCatching {
        if (dataFetcherResult is KLocalDate) {
            dataFetcherResult.toString()
        } else {
            throw CoercingSerializeException("Data fetcher result $dataFetcherResult is not a valid LocalDate.")
        }
    }.getOrElse {
        throw CoercingSerializeException("Data fetcher result $dataFetcherResult cannot be serialized to a String.")
    }

    override fun parseValue(
        input: Any,
        graphQLContext: GraphQLContext,
        locale: Locale
    ): KLocalDate = runCatching {
        KLocalDate.parse(serialize(dataFetcherResult = input, graphQLContext = graphQLContext, locale = locale))
    }.getOrElse {
        throw CoercingParseValueException("Expected valid LocalDate but was $input.")
    }

    override fun parseLiteral(
        input: Value<*>,
        variables: CoercedVariables,
        graphQLContext: GraphQLContext,
        locale: Locale
    ): KLocalDate {
        val dateString = (input as? StringValue)?.value
        return runCatching {
            KLocalDate.parse(dateString!!)
        }.getOrElse {
            throw CoercingParseLiteralException("Expected valid LocalDate literal but was $dateString.")
        }
    }
}