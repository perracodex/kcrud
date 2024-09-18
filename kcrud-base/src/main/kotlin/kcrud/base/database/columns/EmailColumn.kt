/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.database.columns

import kcrud.base.persistence.validators.EmailValidator
import org.jetbrains.exposed.crypt.EncryptedVarCharColumnType
import org.jetbrains.exposed.crypt.Encryptor
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

/**
 * Custom email column with validation.
 *
 * @param name The name of the column in the database.
 * @param length The maximum length of the email column (default is 254).
 * @return A Column<String> representing the email in the Exposed table.
 * @throws IllegalArgumentException if the email format is invalid during data insertion or retrieval.
 */
internal fun Table.email(name: String, length: Int = 254): Column<String> {
    return varchar(name = name, length = length).transform(
        wrap = { it },
        unwrap = { email ->
            EmailValidator.validate(value = email).getOrThrow()
            email
        }
    )
}

/**
 * Custom encrypted email column with validation.
 *
 * @param name The name of the column in the database.
 * @param cipherTextLength Maximum expected length of encrypted email.
 * @param encryptor [Encryptor] responsible for performing encryption and decryption of stored emails.
 * @return A Column<String> representing the email in the Exposed table.
 * @throws IllegalArgumentException if the email format is invalid during data insertion or retrieval.
 */
internal fun Table.encryptedEmail(name: String, cipherTextLength: Int, encryptor: Encryptor): Column<String> {
    return registerColumn(
        name = name,
        type = EncryptedVarCharColumnType(encryptor = encryptor, colLength = cipherTextLength)
    ).transform(
        wrap = { it },
        unwrap = { email ->
            // Validate the plaintext email before it is encrypted and stored.
            EmailValidator.validate(value = email).getOrThrow()
            email
        }
    )
}
