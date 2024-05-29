/*
 * Copyright (c) 2023-Present Perracodex. All rights reserved.
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
fun <T : Any> Table.baseValidColumn(
    name: String,
    textLength: Int,
    validator: IValidator,
    columnTypeProvider: (length: Int) -> ColumnType<T>
): Column<T> {
    // Resolve the target column type using the provided lambda.
    val baseColumn: ColumnType<T> = columnTypeProvider(textLength)

    // Create a custom ColumnType with validation.
    val validColumnType: ColumnType<T> = object : ColumnType<T>() {
        override fun sqlType(): String = baseColumn.sqlType()

        override fun notNullValueToDB(value: T): Any {
            if (validator.validate(value = value) is IValidator.Result.Success) {
                return baseColumn.notNullValueToDB(value = value)
            }

            validator.raise(message = value.toString())
        }

        override fun valueFromDB(value: Any): T? {
            return baseColumn.valueFromDB(value = value)
        }

        override fun nonNullValueToString(value: T): String {
            return baseColumn.nonNullValueToString(value = value)
        }

        override fun nonNullValueAsDefaultString(value: T): String {
            return baseColumn.nonNullValueAsDefaultString(value = value)
        }
    }

    // Register the column with the new custom ColumnType.
    return registerColumn(name = name, type = validColumnType)
}
