/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.settings.config.sections

import kcrud.base.database.service.IsolationLevel
import kcrud.base.infrastructure.env.EnvironmentType
import kcrud.base.settings.config.parser.IConfigSection

/**
 * Database related settings.
 *
 * @property name The name of the database.
 * @property path The database file location.
 * @property jdbcUrl The JDBC url database connection.
 * @property jdbcDriver The JDBC driver class name.
 * @property isolationLevel The database transactions [IsolationLevel].
 * @property transactionRetryAttempts Max retries inside a transaction if a SQLException happens. Overridable per-transaction level.
 * @property transactionRetryMinDelayMs Delay between retries in a transaction if a SQLException happens. Overridable per-transaction level.
 * @property warnLongQueriesDurationMs Threshold to log queries exceeding the threshold with WARN level. Overridable per-transaction level.
 * @property connectionPoolSize The database connection pool size. 0 for no connection pooling.
 * @property minimumPoolIdle The minimum number of idle connections to maintain in the pool.
 * @property connectionPoolTimeoutMs The database connection pool timeout, (ms).
 * @property updateSchemaEnvironments The list f environments under which it is allowed to update the database schema.
 * @property useMigrations Whether to use migrations in order to set the database, or the schema creation via exposed.
 * @property username Optional database username.
 * @property password Optional database password.
 */
data class DatabaseSettings(
    val name: String,
    val path: String,
    val jdbcUrl: String,
    val jdbcDriver: String,
    val isolationLevel: IsolationLevel,
    val transactionRetryAttempts: Int,
    val transactionRetryMinDelayMs: Long,
    val warnLongQueriesDurationMs: Long,
    val connectionPoolSize: Int,
    val minimumPoolIdle: Int,
    val connectionPoolTimeoutMs: Long,
    val updateSchemaEnvironments: List<EnvironmentType>,
    val useMigrations: Boolean,
    val username: String? = null,
    val password: String? = null,
) : IConfigSection
