/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.server.demo

import kotlinx.html.*

/**
 * Builds the HTML for the demo view.
 */
@DemoApi
internal object DemoView {

    /**
     * Builds the HTML for the demo view.
     *
     * @param html The [HTML] builder.
     */
    fun build(html: HTML) {
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
                            value = 0.toString()
                        }
                    }
                    div(classes = "action-records") {
                        div(classes = "timer-placeholder")
                        h3 {
                            id = "totalElements"
                            +(0.toString())
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

                // Page details.
                div(classes = "page-details") {
                    +(
                            "Loaded Pages: 0, " +
                                    "Loaded Records: 0, " +
                                    "Records per Page: 0, " +
                                    "Total Pages: 0, " +
                                    "Total Records: 0"
                            )
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
                    div { +"Work Email" }
                    div { +"Contact Phone" }
                    div { +"Status" }
                    div { +"Modality" }
                }

                // Scrollable content container.
                div(classes = "table-content")
            }
        }
    }
}
