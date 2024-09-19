/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.database.columns

import kcrud.base.persistence.validators.IValidator
import kcrud.base.persistence.validators.ValidationException
import org.jetbrains.exposed.crypt.EncryptedVarCharColumnType
import org.jetbrains.exposed.crypt.Encryptor
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

/**
 * Custom column with validation.
 * For an encrypted column, use instead [encryptedValidVarchar].
 *
 * @param name The name of the column in the database.
 * @param length The maximum length of the value column.
 * @param validator The [IValidator] responsible for validating the value before insertion.
 * @return A Column<String> representing the value in the database table.
 * @throws IllegalArgumentException if the value is invalid during data insertion.
 *
 * @see IValidator
 * @see encryptedValidVarchar
 */
internal fun Table.validVarchar(name: String, length: Int, validator: IValidator<String>): Column<String> {
    return varchar(name = name, length = length).transform(
        wrap = { it },
        unwrap = { value ->
            validator.check(value = value).getOrThrow()
        }
    )
}

/**
 * Custom encrypted column with validation.
 * For a non-encrypted column, use instead [validVarchar].
 *
 * @param name The name of the column in the database.
 * @param length The maximum length of the value column. Will be adjusted to accommodate the encrypted value.
 * @param encryptor The [Encryptor] responsible for performing encryption and decryption of stored values.
 * @param validator The [IValidator] responsible for validating the value before insertion.
 * @return A Column<String> representing the values in the database table.
 * @throws ValidationException if the value is invalid during data insertion.
 *
 * @see IValidator
 * @see validVarchar
 */
internal fun Table.encryptedValidVarchar(
    name: String,
    length: Int,
    encryptor: Encryptor,
    validator: IValidator<String>
): Column<String> {
    return registerColumn(
        name = name,
        type = EncryptedVarCharColumnType(
            encryptor = encryptor,
            colLength = encryptor.maxColLength(inputByteSize = length)
        )
    ).transform(
        wrap = { it },
        unwrap = { value ->
            validator.check(value = value).getOrThrow()
        }
    )
}
