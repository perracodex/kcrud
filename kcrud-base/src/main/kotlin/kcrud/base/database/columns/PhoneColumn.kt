/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.database.columns

import kcrud.base.persistence.validators.PhoneValidator
import org.jetbrains.exposed.crypt.EncryptedVarCharColumnType
import org.jetbrains.exposed.crypt.Encryptor
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

/**
 * Custom phone-number column with validation.
 *
 * @param name The name of the column in the database.
 * @param length The maximum length of the phone-number column (default is 254).
 * @return A Column<String> representing the phone-number in the Exposed table.
 * @throws IllegalArgumentException if the phone-number format is invalid during data insertion or retrieval.
 */
internal fun Table.phone(name: String, length: Int = 254): Column<String> {
    return varchar(name = name, length = length).transform(
        wrap = { it },
        unwrap = { phone ->
            PhoneValidator.validate(value = phone).getOrThrow()
            phone
        }
    )
}

/**
 * Custom encrypted phone-number column with validation.
 *
 * @param name The name of the column in the database.
 * @param cipherTextLength Maximum expected length of encrypted phone-number.
 * @param encryptor [Encryptor] responsible for performing encryption and decryption of stored phone-numbers.
 * @return A Column<String> representing the phone-number in the Exposed table.
 * @throws IllegalArgumentException if the phone-number format is invalid during data insertion or retrieval.
 */
internal fun Table.encryptedPhone(name: String, cipherTextLength: Int, encryptor: Encryptor): Column<String> {
    return registerColumn(
        name = name,
        type = EncryptedVarCharColumnType(encryptor = encryptor, colLength = cipherTextLength)
    ).transform(
        wrap = { it },
        unwrap = { phone ->
            // Validate the plaintext phone-number before it is encrypted and stored.
            PhoneValidator.validate(value = phone).getOrThrow()
            phone
        }
    )
}
