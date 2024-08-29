/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.database.columns

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
internal fun Table.encryptedValidVarChar(
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
