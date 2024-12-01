/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.database.column

import kcrud.core.error.validator.base.IValidator
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

/**
 * Custom column with validation.
 * For an encrypted column, use instead [validEncryptedVarchar].
 *
 * @param name The name of the column in the database.
 * @param length The maximum length of the value column.
 * @param validator The [IValidator] responsible for validating the value before insertion.
 * @return A Column<String> representing the value in the database table.
 * @throws IllegalArgumentException if the value is invalid during data insertion.
 *
 * @see [IValidator]
 * @see [validEncryptedVarchar]
 */
internal fun Table.validVarchar(name: String, length: Int, validator: IValidator<String>): Column<String> {
    return varchar(name = name, length = length).transform(
        wrap = { it },
        unwrap = { value ->
            validator.check(value = value).getOrThrow()
        }
    )
}
