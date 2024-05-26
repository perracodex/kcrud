/*
 * Copyright (c) 2024-Present Perraco. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.database.schema.contact

import kcrud.base.database.custom_columns.encryptedValidVarChar
import kcrud.base.database.schema.base.TimestampedTable
import kcrud.base.database.schema.employee.EmployeeTable
import kcrud.base.persistence.validators.impl.EmailValidator
import kcrud.base.security.utils.EncryptionUtils
import org.jetbrains.exposed.crypt.Encryptor
import org.jetbrains.exposed.crypt.encryptedVarchar
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import java.util.*

/**
 * Database table definition for employee contact details.
 * Demonstrates how to encrypt data in the database,
 * in addition of how to validate column data.
 *
 * Even though the project already has email validation at the serializer level,
 * and also at the service level, this table also demonstrates how to also validate
 * the email at column level with a custom [encryptedValidVarChar] extension function.
 *
 * For the phone we could do the same as for the email, but for the sake of simplicity
 * we just encrypt it with the [encryptedVarchar] extension function, and leave
 * the validation up to the service layer.
 *
 * For encrypted fields, the lengths are larger than the actual length of the data,
 * since the encrypted data will be larger than the original value.
 */
object ContactTable : TimestampedTable(name = "contact") {
    private val encryptor: Encryptor = EncryptionUtils.getEncryptor()

    /**
     * The unique id of the contact record.
     */
    val id: Column<UUID> = uuid(
        name = "contact_id"
    ).autoGenerate()

    /**
     * The id of the employee to which the contact details belong.
     */
    val employeeId: Column<UUID> = uuid(
        name = "employee_id"
    ).references(
        fkName = "fk_contact__employee_id",
        ref = EmployeeTable.id,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.RESTRICT
    )

    /**
     * The contact's email.
     * Must be a valid email.
     */
    val email: Column<String> = encryptedValidVarChar(
        name = "email",
        cipherTextLength = 256,
        encryptor = encryptor,
        validator = EmailValidator
    )

    /**
     * The contact's phone.
     * Must be a valid phone number.
     */
    val phone: Column<String> = encryptedVarchar(
        name = "phone",
        cipherTextLength = encryptor.maxColLength(inputByteSize = 128),
        encryptor = encryptor
    )

    override val primaryKey: Table.PrimaryKey = PrimaryKey(
        firstColumn = id,
        name = "pk_contact_id"
    )

    init {
        index(
            customIndexName = "ix_contact__employee_id",
            isUnique = false,
            columns = arrayOf(employeeId)
        )
    }
}
