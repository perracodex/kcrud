/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.core.plugins

import io.ktor.server.application.*
import io.ktor.server.thymeleaf.*
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver

/**
 * Configures the Thymeleaf plugin for the application used to render HTML templates.
 *
 * #### References
 * - [Thymeleaf Plugin](https://ktor.io/docs/server-thymeleaf.html)
 */
public fun Application.configureThymeleaf() {

    install(plugin = Thymeleaf) {
        setTemplateResolver(
            ClassLoaderTemplateResolver().apply {
                prefix = "/"
                suffix = ".html"
                characterEncoding = "utf-8"
            }
        )
    }
}
