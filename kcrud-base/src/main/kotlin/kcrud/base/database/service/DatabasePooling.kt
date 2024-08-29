/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.database.service

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import kcrud.base.database.annotation.DatabaseAPI
import kcrud.base.settings.config.sections.DatabaseSettings

@DatabaseAPI
internal object DatabasePooling {

    /**
     * Create a HikariDataSource to enable database connection pooling.
     *
     * @param settings The [DatabaseSettings] settings to be used for the database connection pooling.
     * @param isolationLevel The isolation level to use for the database transactions.
     * @param micrometerRegistry Optional [PrometheusMeterRegistry] instance for micro-metrics monitoring.
     *
     * See: [Database Pooling](https://ktor.io/docs/db-connection-pooling-caching.html#connection-pooling)
     */
    fun createDataSource(
        settings: DatabaseSettings,
        isolationLevel: IsolationLevel = IsolationLevel.TRANSACTION_REPEATABLE_READ,
        micrometerRegistry: PrometheusMeterRegistry? = null
    ): HikariDataSource {
        require(value = settings.connectionPoolSize > 0) { "Database connection pooling must be >= 1." }

        val hikariConfig: HikariConfig = HikariConfig().apply {
            driverClassName = settings.jdbcDriver
            jdbcUrl = settings.jdbcUrl
            maximumPoolSize = settings.connectionPoolSize
            connectionTimeout = settings.connectionPoolTimeoutMs
            transactionIsolation = isolationLevel.name
            minimumIdle = settings.minimumPoolIdle
            isAutoCommit = false

            if (!settings.username.isNullOrBlank()) {
                this.username = settings.username
                this.password = settings.password!!
            }

            micrometerRegistry?.let {
                metricRegistry = it
            }

            validate()
        }

        return HikariDataSource(hikariConfig)
    }
}
