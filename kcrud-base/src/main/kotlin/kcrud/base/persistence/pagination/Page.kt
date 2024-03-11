/*
 * Copyright (c) 2023-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.persistence.pagination

import kotlinx.serialization.Serializable
import kotlin.math.max

/**
 * Holds the data for a page of results.

 * @param totalPages The total number of pages available based on the pagination settings.
 * @param pageIndex The current page number, 0-based.
 * @param totalElements Total number of elements in the entire dataset, not just a page.
 * @param elementsPerPage The number of elements per each page.
 * @param elementsInPage The number of elements in the current page.
 * @param isFirst True if serving the first page.
 * @param isLast True if serving the last page.
 * @param hasNext True if there is a next page.
 * @param hasPrevious True if there is a previous page.
 * @param overflow Whether the requested page index is above the total available pages.
 * @param sort The optional sorting that has been applied to the content.
 * @param content The data that forms the content in a page.
 */
@Serializable
open class Page<out T : Any>(
    val totalPages: Int,
    val pageIndex: Int,
    val totalElements: Int,
    val elementsPerPage: Int,
    val elementsInPage: Int,
    val isFirst: Boolean,
    val isLast: Boolean,
    val hasNext: Boolean,
    val hasPrevious: Boolean,
    val overflow: Boolean,
    val sort: List<Pageable.Sort>?,
    val content: List<T>
) {
    companion object {
        /**
         * Factory method to create a new [Page] object.
         *
         * @param content The list of objects for the current page.
         * @param totalElements The total number of elements in the entire dataset, not just the page.
         * @param pageable The pagination information that was used to request the content, or null if none was used.
         * @return A new [Page] instance with the given [content], including a computed page details.
         */
        fun <T : Any> build(content: List<T>, totalElements: Int, pageable: Pageable?): Page<T> {
            // Set default page size.
            val pageSize: Int = pageable?.size.takeIf { it != null && it > 0 } ?: totalElements

            // Calculate total pages, ensuring totalPages is 0 if there are no elements.
            val totalPages: Int = if (totalElements > 0 && pageSize > 0) {
                max(1, (totalElements + pageSize - 1) / pageSize)
            } else {
                0
            }

            // Determine the current page index.
            val pageIndex: Int = pageable?.page ?: 0

            // Adjust pagination state based on total pages and content availability.
            val isFirst: Boolean = (pageIndex == 0) || (totalPages == 0)
            val isLast: Boolean = (pageIndex >= totalPages - 1) || (totalPages == 0)
            val hasNext: Boolean = (pageIndex < totalPages - 1) && (totalPages > 0)
            val hasPrevious: Boolean = (pageIndex > 0) && (totalPages > 0)
            val overflow: Boolean = (pageIndex >= totalPages) && (totalPages > 0)
            val elementsInPage: Int = content.size

            // Construct the Page object with the determined states.
            return Page(
                totalPages = totalPages,
                pageIndex = pageIndex,
                totalElements = totalElements,
                elementsPerPage = pageSize,
                elementsInPage = elementsInPage,
                isFirst = isFirst,
                isLast = isLast,
                hasNext = hasNext,
                hasPrevious = hasPrevious,
                overflow = overflow,
                sort = pageable?.sort,
                content = content
            )
        }
    }
}
