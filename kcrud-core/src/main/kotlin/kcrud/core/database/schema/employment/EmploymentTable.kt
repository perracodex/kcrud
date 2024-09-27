/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.core.database.schema.employment

import kcrud.core.database.columns.autoGenerate
import kcrud.core.database.columns.enumerationById
import kcrud.core.database.columns.kotlinUuid
import kcrud.core.database.columns.references
import kcrud.core.database.schema.base.PeriodTable
import kcrud.core.database.schema.employee.EmployeeTable
import kcrud.core.database.schema.employment.types.EmploymentStatus
import kcrud.core.database.schema.employment.types.WorkModality
import kcrud.core.security.utils.EncryptionUtils
import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.crypt.Encryptor
import org.jetbrains.exposed.crypt.encryptedVarchar
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.date
import kotlin.uuid.Uuid

/**
 * Database table definition for employments.
 *
 * An employee may have multiple employments, which indicates re-hiring.
 */
public object EmploymentTable : PeriodTable(name = "employment") {
    private val encryptor: Encryptor = EncryptionUtils.getEncryptor(type = EncryptionUtils.Type.AT_REST)

    /**
     * The unique id of the employment record.
     */
    public val id: Column<Uuid> = kotlinUuid(
        name = "employment_id"
    ).autoGenerate()

    /**
     * The id of the employee to which the employment belongs.
     */
    public val employeeId: Column<Uuid> = kotlinUuid(
        name = "employee_id"
    ).references(
        ref = EmployeeTable.id,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.RESTRICT,
        fkName = "fk_employment__employee_id"
    ).index(
        customIndexName = "ix_employment__employee_id"
    )

    /**
     * The status of the employment.
     */
    public val status: Column<EmploymentStatus> = enumerationById(
        name = "status_id",
        entries = EmploymentStatus.entries
    )

    /**
     * The date the employment started.
     */
    public val probationEndDate: Column<LocalDate?> = date(
        name = "probation_end_date"
    ).nullable()

    /**
     * The [WorkModality] of the employment.
     */
    public val workModality: Column<WorkModality> = enumerationById(
        name = "work_modality_id",
        entries = WorkModality.entries
    )

    /**
     * Demonstrates how to use an encrypted column.
     *
     * In encrypted fields, the lengths are larger than the actual length of the unencrypted data,
     * since the encrypted final output will be larger than the original unencrypted value.
     * The [Encryptor.maxColLength] method must be used to calculate the maximum length
     * of the encrypted data, passing as input the maximum length of the unencrypted data.
     *
     * Encrypted fields cannot be defined as `unique` at the database level due to varying
     * encrypted outputs for the same data.
     */
    public val sensitiveData: Column<String?> = encryptedVarchar(
        name = "sensitive_data",
        cipherTextLength = encryptor.maxColLength(inputByteSize = 512),
        encryptor = encryptor
    ).nullable()

    /**
     * The table's primary key.
     */
    override val primaryKey: Table.PrimaryKey = PrimaryKey(
        firstColumn = id,
        name = "pk_employment_id"
    )
}
