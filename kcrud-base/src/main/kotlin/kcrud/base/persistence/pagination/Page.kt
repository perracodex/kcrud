/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.persistence.pagination

import kcrud.base.persistence.pagination.Page.Details
import kotlinx.serialization.Serializable

/**
 * Holds the data for a page of results.
 *
 * @param details The pagination [Details] that describe the state of the page.
 * @param content The data that forms the content in a page.
 */
@Serializable
public data class Page<out T : Any>(
    val details: Details,
    val content: List<T>,
) {
    /**
     * The [Page] details that describe the pagination state.
     *
     * Set as a separate data class to simplify the OpenAPI documentation,
     * so each generated Page specification per its concrete type does not
     * have to include the entire Page details attributes.
     *
     * @property totalPages The total number of pages available based on the pagination settings.
     * @property pageIndex The current page number, 0-based.
     * @property totalElements Total number of elements in the entire dataset, not just a page.
     * @property elementsPerPage The number of elements per each page.
     * @property elementsInPage The number of elements in the current page.
     * @property isFirst True if serving the first page.
     * @property isLast True if serving the last page.
     * @property hasNext True if there is a next page.
     * @property hasPrevious True if there is a previous page.
     * @property overflow Whether the requested page index is above the total available pages.
     * @property sort The optional sorting that has been applied to the content.
     */
    @Serializable
    public data class Details(
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
    )

    public companion object {
        /**
         * Factory method to create a new [Page] instance.
         *
         * @param content The list of object data for the page.
         * @param totalElements The total number of elements in the entire dataset, not just the page.
         * @param pageable The pagination information that was used to request the content, or null if none was used.
         * @return A new [Page] instance with the given [content], including a computed page details.
         */
        public fun <T : Any> build(content: List<T>, totalElements: Int, pageable: Pageable?): Page<T> {
            // Set default page size.
            val pageSize: Int = pageable?.size.takeIf { it != null && it > 0 } ?: totalElements

            // Calculate total pages, ensuring totalPages is 0 if there are no elements.
            val totalPages: Int = if (totalElements > 0 && pageSize > 0) {
                ((totalElements + pageSize - 1) / pageSize).coerceAtLeast(minimumValue = 1)
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
                details = Details(
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
                    sort = pageable?.sort
                ),
                content = content
            )
        }
    }
}
