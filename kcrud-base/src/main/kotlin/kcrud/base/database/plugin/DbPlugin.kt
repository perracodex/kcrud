/*
 * Copyright (c) 2024-Present Perraco. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.database.plugin

import io.ktor.server.application.*
import io.micrometer.prometheus.PrometheusMeterRegistry
import kcrud.base.database.service.DatabaseService
import kcrud.base.settings.AppSettings
import org.jetbrains.exposed.sql.Table

/**
 * Configuration for the [DbPlugin].
 */
class DbPluginConfig {
    /** List of tables to be registered with the database. */
    val tables: MutableList<Table> = mutableListOf()

    /** Optional [PrometheusMeterRegistry] instance for micro-metrics monitoring. */
    var micrometerRegistry: PrometheusMeterRegistry? = null

    /**
     * Adds a table to the list of tables to be registered with the database.
     *
     * @param table The [Table] to be added.
     */
    fun addTable(table: Table) {
        tables.add(table)
    }
}

/**
 * Custom Ktor plugin to configure the database.
 */
val DbPlugin = createApplicationPlugin(
    name = "DatabasePlugin",
    createConfiguration = ::DbPluginConfig
) {
    DatabaseService.init(
        settings = AppSettings.database,
        micrometerRegistry = pluginConfig.micrometerRegistry
    ) {
        pluginConfig.tables.forEach { table ->
            addTable(table)
        }
    }
}
