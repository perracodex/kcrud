/*
 * Copyright (c) 2023-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.admin.rbac.views

import kcrud.base.admin.rbac.plugin.annotation.RbacAPI
import kcrud.base.security.service.CredentialService
import kotlinx.html.*

/**
 * Handles generating the login form and managing form submission responses.
 *
 * See: [HTML DSL](https://ktor.io/docs/html-dsl.html)
 */
@RbacAPI
internal object RbacLoginView {

    /** The path for the RBAC login view. */
    const val RBAC_LOGIN_PATH: String = "/rbac/login"

    /** The key for the username field. */
    const val KEY_USERNAME: String = "username"

    /** The key for the password field. */
    const val KEY_PASSWORD: String = "password"

    fun build(html: HTML) {
        with(html) {
            head {
                title { +"RBAC Login" }
                link(rel = "stylesheet", type = "text/css", href = "/static-rbac/admin.css")
            }
            buildForm()
        }
    }

    /**
     * Generates the login form using HTML DSL.
     *
     * See: [HTML DSL](https://ktor.io/docs/html-dsl.html)
     */
    private fun HTML.buildForm() {
        body {
            h1 { +"RBAC Login" }

            form(action = RBAC_LOGIN_PATH, method = FormMethod.post) {
                acceptCharset = "utf-8"
                h4 { +CredentialService.HINT }
                p {
                    label {
                        +"Username:"
                        br()
                        textInput(name = KEY_USERNAME) {
                            classes = setOf("input-style")
                            placeholder = "enter username"
                        }
                    }
                }
                p {
                    label {
                        +"Password:"
                        br()
                        passwordInput(name = KEY_PASSWORD) {
                            classes = setOf("input-style")
                            placeholder = "enter password"
                        }
                    }
                }
                button(type = ButtonType.submit) { +"Login" }
            }
        }
    }
}
