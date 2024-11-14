/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.core.scheduler.service

import kcrud.core.scheduler.service.annotation.SchedulerApi
import kcrud.core.settings.AppSettings
import java.util.*

/**
 * Loads the configuration properties for the task scheduler.
 */
@SchedulerApi
internal object SchedulerConfig {

    /** The configuration properties file, located in the 'resources' directory. */
    private const val PROPERTIES_FILE: String = "quartz.properties"

    /**
     * Returns the configuration properties for the task scheduler.
     * This method modifies the loaded properties to include runtime database settings.
     */
    fun get(): Properties {
        // Load the configuration properties from the quartz.properties file.
        val schema: Properties = loadConfigurationFile()

        // Set the database connection properties.
        val dataSourceName: String = schema["org.quartz.jobStore.dataSource"].toString()
        schema.apply {
            this["org.quartz.dataSource.$dataSourceName.driver"] = AppSettings.database.jdbcDriver
            this["org.quartz.dataSource.$dataSourceName.URL"] = AppSettings.database.jdbcUrl
            this["org.quartz.dataSource.$dataSourceName.user"] = AppSettings.database.username.orEmpty()
            this["org.quartz.dataSource.$dataSourceName.password"] = AppSettings.database.password.orEmpty()
            this["org.quartz.dataSource.$dataSourceName.maxConnections"] = AppSettings.database.connectionPoolSize
        }

        return schema
    }

    /**
     * Loads the configuration properties from the quartz.properties file.
     */
    private fun loadConfigurationFile(): Properties {
        val properties = Properties()
        Thread.currentThread().contextClassLoader.getResourceAsStream(PROPERTIES_FILE)?.use { stream ->
            properties.load(stream)
        }
        return properties
    }
}
