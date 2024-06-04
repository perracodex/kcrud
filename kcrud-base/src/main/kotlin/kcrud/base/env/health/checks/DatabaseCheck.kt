/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.env.health.checks

import com.zaxxer.hikari.HikariDataSource
import kcrud.base.env.health.annotation.HealthCheckAPI
import kcrud.base.env.health.checks.DatabaseCheck.*
import kcrud.base.settings.AppSettings
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.name
import org.jetbrains.exposed.sql.statements.api.ExposedConnection
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.vendors.currentDialect

/**
 * Represents the database health check.
 *
 * @property errors The list of errors found during the check.
 * @property alive True if the database is alive, false otherwise.
 * @property datasource The [Datasource] information.
 * @property connectionTest The [ConnectionTest] information.
 * @property configuration The database [Configuration].
 * @property tables The list of tables in the database.
 */
@HealthCheckAPI
@Serializable
data class DatabaseCheck(
    val errors: MutableList<String>,
    val alive: Boolean,
    val datasource: Datasource?,
    val connectionTest: ConnectionTest?,
    val configuration: Configuration,
    val tables: List<String>
) {
    constructor(alive: Boolean, connectionTest: ConnectionTest?, datasource: Datasource?, tables: List<String>) : this(
        errors = mutableListOf(),
        alive = alive,
        connectionTest = connectionTest,
        datasource = datasource,
        configuration = Configuration(),
        tables = tables
    )

    init {
        val className: String? = this::class.simpleName

        if (!alive) {
            errors.add("$className. Database is not responding. $configuration")
        }

        if (datasource == null) {
            errors.add("$className. Undefined datasource. $configuration")
        }

        if (tables.isEmpty()) {
            errors.add("$className. No tables detected. $configuration")
        }

        connectionTest?.let {
            if (!it.established) {
                errors.add("$className. Database connection not established.")
            }

            if (it.isReadOnly) {
                errors.add("$className. Database connection is read-only.")
            }
        } ?: errors.add("$className. Unable to test database connection.")
    }

    /**
     * Represents the database connection test.
     *
     * @property established True if the connection is established, false otherwise.
     * @property isReadOnly Whether the connection is in read-only mode.
     * @property name The name of the database obtained from its connection URL.
     * @property version The database version.
     * @property dialect The database dialect name.
     * @property url The connection URL for the database.
     * @property vendor The name of the database based on the name of the underlying JDBC driver.
     * @property autoCommit Whether the connection is in auto-commit mode.
     * @property catalog The name of the connection's catalog.
     */
    @Serializable
    data class ConnectionTest(
        val established: Boolean,
        val isReadOnly: Boolean,
        val name: String,
        val version: String,
        val dialect: String,
        val url: String,
        val vendor: String,
        val autoCommit: Boolean,
        val catalog: String,
    ) {
        /**
         * Represents the result of a connection test.
         *
         * @property output The [ConnectionTest] object if the test was successful.
         * @property error The error message if the test failed.
         */
        data class Result(val output: ConnectionTest?, val error: String?)

        companion object {
            /**
             * Builds a [ConnectionTest] object from a [Database] object.
             *
             * @param database The source [Database] object to test.
             * @return A pair of the [ConnectionTest] object if successful, and an error message if not.
             */
            fun build(database: Database?): Result = runCatching {
                requireNotNull(database) { "Database must not be null" }

                val connector: ExposedConnection<*> = database.connector()

                try {
                    val test = ConnectionTest(
                        established = !connector.isClosed,
                        isReadOnly = connector.readOnly,
                        name = database.name,
                        version = database.version.toString(),
                        dialect = transaction(db = database) { currentDialect.name },
                        url = database.url,
                        vendor = database.vendor,
                        autoCommit = connector.autoCommit,
                        catalog = connector.catalog
                    )

                    Result(output = test, error = null)
                } finally {
                    connector.close()
                }
            }.getOrElse { e ->
                Result(output = null, error = "${ConnectionTest::class.simpleName}. ${e.message}")
            }
        }
    }

    /**
     * Represents the datasource information.
     *
     * @property isPoolRunning Whether the pool is started and is not suspended or shutdown.
     * @property totalConnections The total number of connections in the pool
     * @property activeConnections The current number of active (in-use) connections in the pool.
     * @property idleConnections The current number of idle connections in the pool.
     * @property threadsAwaitingConnection The number of threads awaiting connection.
     * @property connectionTimeout The maximum number of milliseconds that a client will wait for a connection from the pool, (ms).
     * @property maxLifetime The maximum connection lifetime, (ms).
     * @property keepaliveTime The interval in which connections is tested for aliveness, (ms).
     * @property maxPoolSize Maximum number of connections kept in the pool, including both idle and in-use connections.
     */
    @Serializable
    data class Datasource(
        val isPoolRunning: Boolean,
        val totalConnections: Int,
        val activeConnections: Int,
        val idleConnections: Int,
        val threadsAwaitingConnection: Int,
        val connectionTimeout: Long,
        val maxLifetime: Long,
        val keepaliveTime: Long,
        val maxPoolSize: Int
    ) {
        companion object {
            /**
             * Builds a [Datasource] instance from a [HikariDataSource] source.
             *
             * @param datasource The source [HikariDataSource] to build from.
             * @return The build [Datasource] instance.
             */
            fun build(datasource: HikariDataSource?): Datasource? {
                return datasource?.let {
                    Datasource(
                        isPoolRunning = it.isRunning,
                        totalConnections = it.hikariPoolMXBean?.totalConnections ?: 0,
                        activeConnections = it.hikariPoolMXBean?.activeConnections ?: 0,
                        idleConnections = it.hikariPoolMXBean?.idleConnections ?: 0,
                        threadsAwaitingConnection = it.hikariPoolMXBean?.threadsAwaitingConnection ?: 0,
                        connectionTimeout = it.connectionTimeout,
                        maxLifetime = it.maxLifetime,
                        keepaliveTime = it.keepaliveTime,
                        maxPoolSize = it.maximumPoolSize
                    )
                }
            }
        }
    }

    /**
     * Represents the database configuration.
     *
     * @property poolSize The database connection pool size. 0 if no connection pooling.
     * @property connectionTimeout The database connection pool timeout, (ms).
     * @property transactionRetryAttempts Max retries inside a transaction if a SQLException happens.
     * @property warnLongQueriesDurationMs Threshold to log queries exceeding the threshold with WARN level.
     * @property jdbcDriver The JDBC driver class name.
     * @property jdbcUrl The JDBC url database connection.
     */
    @Serializable
    data class Configuration(
        val poolSize: Int = AppSettings.database.connectionPoolSize,
        val connectionTimeout: Long = AppSettings.database.connectionPoolTimeoutMs,
        val transactionRetryAttempts: Int = AppSettings.database.transactionMaxAttempts,
        val warnLongQueriesDurationMs: Long = AppSettings.database.warnLongQueriesDurationMs,
        val jdbcDriver: String = AppSettings.database.jdbcDriver,
        val jdbcUrl: String = AppSettings.database.jdbcUrl,
    )
}
