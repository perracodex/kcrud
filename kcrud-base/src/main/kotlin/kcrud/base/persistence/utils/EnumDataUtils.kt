/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.persistence.utils

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import kotlin.enums.enumEntries

/**
 * Example in which each item has an id,
 * which is the actual value that will be stored in the database,
 * instead of the name of the enum item.
 *
 * @property id The integer ID of the enum item.
 *
 * @see enumerationById
 * @see getEnumById
 */
public interface IEnumWithId {
    public val id: Int
}

/**
 * Creates a column in an Exposed Table to store enum values by their integer unique IDs,
 * simplifying the process of defining a column in an Exposed table that corresponds
 * to an enum type, where the enum values are stored in the database as integers.
 * It uses a transformation approach to map integers to enum instances, leveraging the
 * integer column type in Exposed.
 *
 * Usage example:
 * ```
 * enum class SomeEnum(override val id: Int) : EnumWithId {
 *     ELEMENT_A(100), ELEMENT_B(101), ...
 * }
 *
 * object SomeTable : Table() {
 *     val status: Column<SomeEnum> = enumerationById(
 *         name = "field_name",
 *         fromId = ::getEnumById
 *     )
 *     ...
 * }
 * ```
 * @param E The enum class type. This class must implement the IEnumWithId interface.
 * @param name The name of the column in the database.
 * @param fromId A function that takes an integer ID and returns the corresponding enum value.
 *               It should return null if the ID does not correspond to any enum value.
 * @return A Column<E> representing the enum in the Exposed table.
 * @throws IllegalArgumentException if an unknown enum id is encountered in the database.
 *
 * @see IEnumWithId
 * @see getEnumById
 */
internal fun <E : IEnumWithId> Table.enumerationById(
    name: String,
    fromId: (Int) -> E?
): Column<E> {
    return integer(name = name).transform(
        wrap = { dbId -> fromId(dbId) ?: throw IllegalArgumentException("Unknown enum id: $dbId") },
        unwrap = { enum -> enum.id }
    )
}

/**
 * Retrieves an enum instance of type `E` by its ID.
 *
 * Searches through all entries of an enum implementing `IEnumWithId`
 * and returns the one where the ID matches the specified value.
 *
 * @param id The ID of the enum to retrieve.
 * @return The matching enum instance or `null` if no match is found.
 *
 * @see IEnumWithId
 * @see enumerationById
 */
public inline fun <reified E> getEnumById(id: Int): E? where E : Enum<E>, E : IEnumWithId {
    return enumEntries<E>().firstOrNull { enum -> enum.id == id }
}
