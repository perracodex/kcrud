/*
 * Copyright (c) 2023-Present Perraco. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.database.custom_columns

import kcrud.base.persistence.validators.IValidator
import org.jetbrains.exposed.crypt.EncryptedVarCharColumnType
import org.jetbrains.exposed.crypt.Encryptor
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

/**
 * Creates an encrypted, validated VARCHAR column.
 *
 * @param name Column name in the database.
 * @param cipherTextLength Maximum length of the unencrypted text.
 * @param encryptor Encryptor for data encryption.
 * @param validator Validator to ensure data integrity.
 * @return Encrypted and validated column of type [Column]<[String]>.
 */
fun Table.encryptedValidVarChar(
    name: String,
    cipherTextLength: Int,
    encryptor: Encryptor,
    validator: IValidator
): Column<String> {
    return baseValidColumn(
        name = name,
        textLength = cipherTextLength,
        validator = validator
    ) { length ->
        EncryptedVarCharColumnType(
            encryptor = encryptor,
            colLength = encryptor.maxColLength(inputByteSize = length)
        )
    }
}
