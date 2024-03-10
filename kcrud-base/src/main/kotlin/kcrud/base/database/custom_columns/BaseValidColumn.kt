/*
 * Copyright (c) 2023-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.database.custom_columns

import kcrud.base.persistence.validators.IValidator
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Table

/**
 * Base function for creating validated columns with custom types.
 *
 * Intended as a foundational utility for defining columns, it combines custom column types
 * with validation logic. Use this function as a base to create specific column types
 * (like encrypted or plain text) with additional validation.
 *
 * @param name Name of the database column.
 * @param textLength Expected text length for the column.
 * @param columnTypeProvider Provides ColumnType based on textLength.
 * @param validator Validates the column data.
 * @return [Column]<[String]> with specified characteristics.
 */
fun Table.baseValidColumn(
    name: String,
    textLength: Int,
    validator: IValidator,
    columnTypeProvider: (length: Int) -> ColumnType
): Column<String> {
    // Resolve the target column type using the provided lambda.
    val baseColumnType: ColumnType = columnTypeProvider(textLength)

    // Create a custom ColumnType with validation.
    val validColumnType: ColumnType = object : ColumnType() {
        override fun sqlType(): String = baseColumnType.sqlType()

        override fun notNullValueToDB(value: Any): Any {
            if (value is String && validator.validate(value = value) is IValidator.Result.Success) {
                return baseColumnType.notNullValueToDB(value = value)
            }

            validator.raise(message = value.toString())
        }

        override fun valueFromDB(value: Any): Any = baseColumnType.valueFromDB(value = value)
        override fun nonNullValueToString(value: Any): String = baseColumnType.nonNullValueToString(value = value)
    }

    // Register the column with the new custom ColumnType.
    return registerColumn(name = name, type = validColumnType)
}
