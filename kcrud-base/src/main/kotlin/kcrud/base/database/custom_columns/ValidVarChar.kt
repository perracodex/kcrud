/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.database.custom_columns

import kcrud.base.persistence.validators.IValidator
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.VarCharColumnType

/**
 * Creates a VARCHAR column with custom validation.
 *
 * This function utilizes `baseValidatedColumn` to create a standard VARCHAR column
 * with additional validation logic applied. It's useful when needing to store string data
 * that conforms to specific validation rules defined by the provided `IValidator`.
 *
 * @param name The name of the column in the database.
 * @param textLength The maximum length of the VARCHAR column.
 * @param validator An instance of [IValidator] for validating the column data.
 * @return A [Column]<[String]> representing a VARCHAR column with custom validation.
 */
fun Table.validVarChar(
    name: String,
    textLength: Int,
    validator: IValidator
): Column<String> {
    return baseValidColumn(
        name = name,
        textLength = textLength,
        validator = validator
    ) { length ->
        VarCharColumnType(colLength = length)
    }
}
