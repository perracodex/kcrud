/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.persistence.utils

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.Table.Dual.clientDefault
import org.jetbrains.exposed.sql.vendors.MariaDBDialect
import org.jetbrains.exposed.sql.vendors.currentDialect
import java.sql.ResultSet
import java.util.*
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

/**
 * Converts a [String] to a [Uuid] or returns null if the string is not a valid [Uuid].
 *
 * @return The [Uuid] representation of the string, or null if the string is null or is not a valid [Uuid].
 */
public fun String?.toUuidOrNull(): Uuid? {
    if (this.isNullOrBlank()) return null
    return try {
        Uuid.parse(uuidString = this)
    } catch (e: IllegalArgumentException) {
        null
    }
}

/**
 * Converts a given string to a [Uuid] object.
 *
 * @return a [Uuid] object converted from the string representation.
 * @throws IllegalArgumentException if the string is not a valid [Uuid].
 */
public fun String?.toUuid(): Uuid {
    return try {
        Uuid.parse(uuidString = this!!)
    } catch (e: IllegalArgumentException) {
        throw IllegalArgumentException("String '$this' is not a valid Uuid.")
    }
}

/**
 * Custom Exposed column type for kotlin's [Uuid].
 */
internal class UuidColumnType : ColumnType<Uuid>() {
    override fun sqlType(): String = currentDialect.dataTypeProvider.uuidType()

    override fun valueFromDB(value: Any): Uuid = when {
        value is Uuid -> value
        value is UUID -> value.toKotlinUuid()
        value is ByteArray -> Uuid.fromByteArray(value)
        value is String && value.matches(uuidRegexp) -> Uuid.parse(value)
        value is String -> Uuid.fromByteArray(value.toByteArray())
        else -> error("Unexpected value of type Uuid: $value of ${value::class.qualifiedName}")
    }

    override fun notNullValueToDB(value: Uuid): Any = currentDialect.dataTypeProvider.uuidToDB(value.toJavaUuid())

    override fun nonNullValueToString(value: Uuid): String = "'$value'"

    override fun readObject(rs: ResultSet, index: Int): Any? = when (currentDialect) {
        is MariaDBDialect -> rs.getBytes(index)
        else -> super.readObject(rs, index)
    }

    internal companion object {
        private val uuidRegexp =
            Regex("[0-9A-F]{8}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{12}", RegexOption.IGNORE_CASE)
    }
}

/**
 * Extension function for creating a [Uuid] column.
 */
internal fun Table.kotlinUuid(name: String): Column<Uuid> = registerColumn(name, UuidColumnType())

/**
 * Extension function for auto-generating [Uuid] values.
 */
@JvmName("autoGenerateKotlinUuid")
internal fun Column<Uuid>.autoGenerate(): Column<Uuid> = clientDefault { Uuid.random() }

/**
 *  Extension function for setting up [Uuid] foreign key references.
 */
internal fun Column<Uuid>.references(
    ref: Column<Uuid>,
    onDelete: ReferenceOption,
    onUpdate: ReferenceOption,
    fkName: String
): Column<Uuid> {
    this.foreignKey = ForeignKeyConstraint(
        target = ref,
        from = this,
        onUpdate = onUpdate,
        onDelete = onDelete,
        name = fkName
    )
    return this
}
