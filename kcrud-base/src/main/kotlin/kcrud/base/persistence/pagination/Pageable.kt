/*
 * Copyright (c) 2023-Present Perracodex. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.persistence.pagination

import kcrud.base.persistence.pagination.Pageable.Sort
import kotlinx.serialization.Serializable

/**
 * Input parameters for pagination.
 *
 * @property page The 0-based page index.
 * @property size The size of the page to be returned. 0 means all elements.
 * @property sort The optional [Sort] order to apply to the results.
 */
@Serializable
data class Pageable(
    val page: Int,
    val size: Int,
    val sort: List<Sort>? = null
) {
    init {
        require(value = (page >= 0)) { "Page index must be >= 0." }
        require(value = (size >= 0)) { "Page size must be >= 0. (0 means all elements)." }
    }

    /**
     * Sorting direction.
     */
    enum class Direction {
        /** Ascending sorting direction. */
        ASC,

        /** Descending sorting direction. */
        DESC
    }

    /**
     * Sorting order for a field.
     *
     * @property table Optional name of the table the field belongs to. Used to avoid ambiguity.
     * @property field The name of the field to sort by.
     * @property direction The direction of the sorting.
     */
    @Serializable
    data class Sort(
        val table: String? = null,
        val field: String,
        val direction: Direction
    ) {
        init {
            require(value = field.isNotBlank()) { "The sorting field name must not be blank." }
        }
    }
}
