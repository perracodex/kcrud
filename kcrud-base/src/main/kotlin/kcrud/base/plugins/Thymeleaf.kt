/*
 * Copyright (c) 2024-Present Perracodex. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.plugins

import io.ktor.server.application.*
import io.ktor.server.thymeleaf.*
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver

/**
 * Configures the Thymeleaf plugin for the application used to render HTML templates.
 *
 * See: [Ktor Thymeleaf Plugin](https://ktor.io/docs/server-thymeleaf.html)
 */
fun Application.configureThymeleaf() {

    install(plugin = Thymeleaf) {
        setTemplateResolver(ClassLoaderTemplateResolver().apply {
            prefix = "templates/"
            suffix = ".html"
            characterEncoding = "utf-8"
        })
    }
}
