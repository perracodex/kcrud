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
            // Specifies the class name of the JDBC driver to be used,
            // and JDBC URL for the database connection.
            driverClassName = settings.jdbcDriver
            jdbcUrl = settings.jdbcUrl

            // Maximum number of connections in the pool.
            maximumPoolSize = settings.connectionPoolSize

            // Maximum wait time in milliseconds for an application component to get a connection from the pool.
            connectionTimeout = settings.connectionPoolTimeoutMs

            // Transaction isolation level for the connections within the pool.
            transactionIsolation = isolationLevel.name

            // Minimum number of idle connections maintained by HikariCP in the pool.
            minimumIdle = settings.minimumPoolIdle

            // Disables auto-commit on connections to allow explicit transaction management,
            // thereby ensuring each transaction is atomic and preventing automatic commits
            // after each individual statement.
            isAutoCommit = false

            // Database credentials for authentication.
            if (!settings.username.isNullOrBlank()) {
                check(!settings.password.isNullOrBlank()) { "Database password must be provided when username is set." }
                this.username = settings.username
                this.password = settings.password
            }

            // Integrates a micrometer registry for monitoring and metrics.
            micrometerRegistry?.let {
                metricRegistry = it
            }

            validate()
        }

        return HikariDataSource(hikariConfig)
    }
}
