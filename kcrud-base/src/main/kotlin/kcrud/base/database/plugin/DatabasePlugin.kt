/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.database.plugin

import io.ktor.server.application.*
import kcrud.base.database.service.DatabaseService
import kcrud.base.settings.AppSettings
import org.jetbrains.exposed.sql.Table

/**
 * Configuration for the [DatabasePlugin].
 */
class DatabasePluginConfig {
    val tables: MutableList<Table> = mutableListOf()

    fun addTable(table: Table) {
        tables.add(table)
    }
}

/**
 * Custom Ktor plugin to configure the database.
 */
val DatabasePlugin = createApplicationPlugin(
    name = "DatabasePlugin",
    createConfiguration = ::DatabasePluginConfig
) {
    DatabaseService.init(settings = AppSettings.database) {
        pluginConfig.tables.forEach { table ->
            addTable(table)
        }
    }
}
