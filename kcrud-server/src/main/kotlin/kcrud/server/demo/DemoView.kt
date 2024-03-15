/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.server.demo

import kcrud.base.persistence.pagination.Page
import kcrud.domain.employment.entity.EmploymentEntity
import kotlinx.html.*
import java.text.NumberFormat
import java.util.*

/**
 * Builds the HTML for the demo view.
 */
@DemoAPI
object DemoView {

    fun build(html: HTML, page: Page<EmploymentEntity>) {
        with(html) {
            head {
                title { +"Demo" }
                link(rel = "stylesheet", type = "text/css", href = "/static-demo/style.css")
                script(src = "/static-demo/infinite-scroll.js", type = "text/javascript") {}
            }
            body {
                div(classes = "actions-container") {
                    div(classes = "page-size-container") {
                        label {
                            htmlFor = "pageSize"
                            +"Page Size"
                        }
                        input(classes = "page-size", type = InputType.number) {
                            id = "pageSize"
                            placeholder = "Page Size"
                            value = page.elementsPerPage.toString()
                        }
                    }
                    div(classes = "action-records") {
                        div(classes = "timer-placeholder")
                        h3 {
                            id = "totalElements"
                            +page.totalElements.formatNumberWithThousands()
                        }
                        input {
                            type = InputType.number
                            id = "numberRecords"
                            placeholder = "Number of Records"
                            value = "1000"
                        }
                        button(classes = "create-records", type = ButtonType.button) {
                            id = "createRecordsButton"
                            +"Create"
                        }
                        button(classes = "json-dump", type = ButtonType.button) {
                            id = "jsonDumpButton"
                            +"Json"
                        }
                        button(classes = "delete-records", type = ButtonType.button) {
                            id = "deleteRecordsButton"
                            +"Delete All"
                        }
                    }
                }

                // Page index  starts from 1.
                // Calculate the total number of records loaded up to the previous page.
                val totalRecordsUpToPreviousPage = (page.pageIndex - 1) * page.elementsPerPage
                // Add the number of records in the current page.
                val totalLoadedRecords = totalRecordsUpToPreviousPage + page.content.size

                // Page details.
                div(classes = "page-details") {
                    +("Loaded Pages: ${page.pageIndex}, " +
                            "Loaded Records: ${totalLoadedRecords}, " +
                            "Records per Page: ${page.elementsPerPage}, " +
                            "Total Pages: ${page.totalPages}, " +
                            "Total Records: ${page.totalElements}")
                }

                // Static header.
                div(classes = "table-header") {
                    div { +"Avatar" }
                    div { +"#" }
                    div { +"Name" }
                    div { +"Age" }
                    div { +"Date of Birth" }
                    div { +"Honorific" }
                    div { +"Marital Status" }
                    div { +"Email" }
                    div { +"Phone" }
                    div { +"Status" }
                    div { +"Modality" }
                }

                // Scrollable content container.
                div(classes = "table-content")
            }
        }
    }

    private fun Int.formatNumberWithThousands(): String {
        val formatter: NumberFormat = NumberFormat.getNumberInstance(Locale.US)
        return formatter.format(this)
    }
}
