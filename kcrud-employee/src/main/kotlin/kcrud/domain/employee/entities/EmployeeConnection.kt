/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

@file:Suppress("unused")

package kcrud.domain.employee.entities

import kcrud.base.persistence.pagination.Page
import kcrud.base.persistence.pagination.Pageable
import kotlinx.serialization.Serializable

/**
 * Paginated employee, suitable for GraphQL queries.
 *
 * For REST this is not needed, as a [Page] with the [EmployeeEntity] type
 * can be directly serialized.
 *
 * Graphql on the other hand would need to return this class instead,
 * as it does not support generics.
 *
 * @param page The [Page] to wrap.
 */
@Serializable
data class EmployeeConnection(private val page: Page<EmployeeEntity>) {

    /** The total number of pages available based on the pagination settings. */
    val totalPages: Int get() = page.totalPages

    /** The current page number (usually starting from 1). */
    val pageIndex: Int get() = page.pageIndex

    /** Total number of elements in the entire dataset, not just a page. */
    val totalElements: Int get() = page.totalElements

    /** The number of elements per each page. */
    val elementsPerPage: Int get() = page.elementsPerPage

    /** The number of elements in the current page. */
    val elementsInPage: Int get() = page.elementsInPage

    /** True if serving the first page. */
    val isFirst: Boolean get() = page.isFirst

    /** True if serving the last page. */
    val isLast: Boolean get() = page.isLast

    /** True if there is a next page. */
    val hasNext: Boolean get() = page.hasNext

    /** True if there is a previous page. */
    val hasPrevious: Boolean get() = page.hasPrevious

    /** Whether the requested page index is above the total available pages. */
    val overflow: Boolean get() = page.overflow

    /** The optional sorting that has been applied to the content. */
    val sort: List<Pageable.Sort>? get() = page.sort

    /** The data that forms the content of a page. */
    val content: List<EmployeeEntity> get() = page.content
}
