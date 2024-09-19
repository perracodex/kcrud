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
 *
 * @param name The name of the column in the database.
 * @param length The maximum length of the value column.
 * @return A Column<String> representing the value in the database table.
 * @throws IllegalArgumentException if the value is invalid during data insertion.
 */
@Suppress("unused")
internal fun Table.validVarchar(name: String, length: Int, validator: IValidator): Column<String> {
    return varchar(name = name, length = length).transform(
        wrap = { it },
        unwrap = { value ->
            validator.check(value = value).getOrThrow()
        }
    )
}

/**
 * Custom encrypted column with validation.
 *
 * @param name The name of the column in the database.
 * @param cipherTextLength Maximum expected length of encrypted value.
 * @param encryptor [Encryptor] responsible for performing encryption and decryption of stored values.
 * @return A Column<String> representing the values in the database table.
 * @throws ValidationException if the value is invalid during data insertion.
 */
internal fun Table.encryptedValidVarchar(
    name: String,
    validator: IValidator,
    cipherTextLength: Int,
    encryptor: Encryptor
): Column<String> {
    return registerColumn(
        name = name,
        type = EncryptedVarCharColumnType(encryptor = encryptor, colLength = cipherTextLength)
    ).transform(
        wrap = { it },
        unwrap = { value ->
            validator.check(value = value).getOrThrow()
        }
    )
}
