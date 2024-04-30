/*
 * Copyright (c) 2024-Present Perraco. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.env.health.checks

import com.zaxxer.hikari.HikariDataSource
import kcrud.base.env.health.annotation.HealthCheckAPI
import kcrud.base.settings.AppSettings
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.name
import org.jetbrains.exposed.sql.statements.api.ExposedConnection

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
        companion object {
            fun build(database: Database?): ConnectionTest? {
                return database?.let {
                    val connector: ExposedConnection<*> = it.connector()
                    try {
                        return ConnectionTest(
                            established = !connector.isClosed,
                            isReadOnly = connector.readOnly,
                            name = it.name,
                            version = it.version.toString(),
                            dialect = it.dialect.name,
                            url = it.url,
                            vendor = it.vendor,
                            autoCommit = connector.autoCommit,
                            catalog = connector.catalog
                        )
                    } finally {
                        connector.close()
                    }
                }
            }
        }
    }

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
