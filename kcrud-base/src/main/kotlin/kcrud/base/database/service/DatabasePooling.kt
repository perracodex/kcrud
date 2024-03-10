/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.database.service

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kcrud.base.database.annotation.DatabaseAPI
import kcrud.base.plugins.appMicrometerRegistry
import kcrud.base.settings.config.sections.DatabaseSettings

@DatabaseAPI
internal object DatabasePooling {

    /**
     * Create a HikariDataSource to enable database connection pooling.
     *
     * See: [Database Pooling](https://ktor.io/docs/connection-pooling-caching.html#connection-pooling)
     */
    fun createDataSource(settings: DatabaseSettings): HikariDataSource {
        require(value = settings.connectionPoolSize > 0) { "Database connection pooling must be >= 1." }

        return HikariDataSource(HikariConfig().apply {
            driverClassName = settings.jdbcDriver
            jdbcUrl = settings.jdbcUrl
            maximumPoolSize = settings.connectionPoolSize
            connectionTimeout = settings.connectionPoolTimeoutMs
            transactionIsolation = settings.isolationLevel.name
            minimumIdle = settings.minimumPoolIdle
            isAutoCommit = false

            if (!settings.username.isNullOrBlank()) {
                this.username = settings.username
                this.password = settings.password!!
            }

            metricRegistry = appMicrometerRegistry

            validate()
        })
    }
}
