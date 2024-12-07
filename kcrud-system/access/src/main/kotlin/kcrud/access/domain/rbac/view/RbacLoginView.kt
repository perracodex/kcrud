/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.access.domain.rbac.view

import kcrud.access.credential.CredentialService
import kcrud.access.domain.rbac.annotation.RbacApi
import kotlinx.html.*

/**
 * Handles generating the login form and managing form submission responses.
 *
 * #### References
 * [HTML DSL](https://ktor.io/docs/server-html-dsl.html)
 */
@RbacApi
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
                link(rel = "stylesheet", type = "text/css", href = "/static-rbac/dashboard.css")
            }
            buildForm()
        }
    }

    /**
     * Generates the login form using HTML DSL.
     *
     * See: [HTML DSL](https://ktor.io/docs/server-html-dsl.html)
     */
    private fun HTML.buildForm() {
        body {
            h2(classes = "header") {
                +"RBAC Login"
            }

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
