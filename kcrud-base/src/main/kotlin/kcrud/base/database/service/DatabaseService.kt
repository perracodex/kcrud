/*
 * Copyright (c) 2024-Present Perraco. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.database.service

import com.zaxxer.hikari.HikariDataSource
import kcrud.base.database.annotation.DatabaseAPI
import kcrud.base.env.Tracer
import kcrud.base.env.health.annotation.HealthCheckAPI
import kcrud.base.env.health.checks.DatabaseCheck
import kcrud.base.settings.AppSettings
import kcrud.base.settings.config.sections.DatabaseSettings
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.configuration.FluentConfiguration
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DatabaseConfig
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.vendors.currentDialect
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Manages database configurations and provides utility methods for database maintenance,
 * serving as a centralized point for setting up database connections, and other
 * database-related configurations.
 *
 * See: [Exposed](https://github.com/JetBrains/Exposed/wiki)
 */
@OptIn(DatabaseAPI::class)
object DatabaseService {
    private val tracer = Tracer<DatabaseService>()

    private var _database: Database? = null
    val database: Database
        get() = _database ?: throw IllegalStateException("Database not initialized.")

    private var hikariDataSource: HikariDataSource? = null

    /**
     * Initializes the database connection based on the provided mode and database type.
     *
     * @param settings The database settings to use.
     * @param schemaSetup Optional lambda to setup the database schema.
     */
    fun init(settings: DatabaseSettings, schemaSetup: (SchemaBuilder.() -> Unit)? = null) {

        buildDatabase(settings = settings)

        // Establishes a database connection.
        // If a connection pool size is specified, a HikariCP DataSource is configured to manage the pool
        val databaseInstance: Database = if (settings.connectionPoolSize > 0) {
            val dataSource: HikariDataSource = DatabasePooling.createDataSource(settings = settings)
            hikariDataSource = dataSource
            connectDatabase(settings = settings, datasource = dataSource)
        } else {
            connectDatabase(settings = settings)
        }

        schemaSetup?.let {
            tracer.info("Setting database schema.")
            val schemaBuilder = SchemaBuilder()
            schemaSetup.invoke(schemaBuilder)
            setupDatabaseSchema(
                database = databaseInstance,
                schemaBuilder = schemaBuilder,
                settings = settings
            )
        }

        _database = databaseInstance

        tracer.info("Database ready.")
    }

    /**
     * Opens a database connection using the provided settings.
     *
     * @param settings The database settings to use.
     * @param datasource Optional HikariCP DataSource to use for the connection.
     * @return The database instance.
     */
    private fun connectDatabase(settings: DatabaseSettings, datasource: HikariDataSource? = null): Database {
        val databaseConfig = DatabaseConfig {
            defaultIsolationLevel = settings.isolationLevel.id
            defaultRepetitionAttempts = settings.transactionRetryAttempts
            defaultMinRepetitionDelay = settings.transactionRetryMinDelayMs
            warnLongQueriesDuration = settings.warnLongQueriesDurationMs
        }

        return datasource?.let {
            Database.connect(
                datasource = it,
                databaseConfig = databaseConfig
            )
        } ?: run {
            if (settings.username.isNullOrBlank()) {
                Database.connect(
                    url = settings.jdbcUrl,
                    driver = settings.jdbcDriver,
                    databaseConfig = databaseConfig
                )
            } else {
                Database.connect(
                    url = settings.jdbcUrl,
                    driver = settings.jdbcDriver,
                    user = settings.username,
                    password = settings.password!!,
                    databaseConfig = databaseConfig
                )
            }
        }
    }

    /**
     * Creates the database schema if such does not exist.
     *
     * @param database The database instance to use.
     * @param schemaBuilder The schema builder to generate the database schema.
     * @param settings The target [DatabaseSettings] to be used for the migration.
     */
    private fun setupDatabaseSchema(
        database: Database,
        schemaBuilder: SchemaBuilder,
        settings: DatabaseSettings
    ) {
        if (AppSettings.database.updateSchemaEnvironments.contains(AppSettings.runtime.environment)) {
            transaction(db = database) {
                if (AppSettings.database.useMigrations) {
                    runMigrations(settings = settings)
                } else {
                    schemaBuilder.createTables()
                }
            }
        } else {
            tracer.info("Database schema update skipped for environment: ${AppSettings.runtime.environment}.")
        }
    }

    /**
     * Applies database migrations with the provided database connection details
     * by running the migration scripts found in the default locations.
     *
     * Note that this should never be part of the server execution.
     * Instead, should be decoupled and be executed as a completely independent maintenance step.
     * It is added here just as an example to show how to use Flyway.
     *
     * It is assumed that the migration scripts are located in the `db/migration`
     * directory within the `resources` folder.
     *
     * @param settings The target [DatabaseSettings] to be used for the migration.
     */
    private fun runMigrations(settings: DatabaseSettings) {
        val configuration: FluentConfiguration = Flyway.configure()
        val flyway: Flyway = configuration.dataSource(
            settings.jdbcUrl,
            settings.username,
            settings.password
        ).load()

        flyway.migrate()
    }

    /**
     * Checks whether the database is alive.
     */
    private fun ping(): Boolean {
        return try {
            transaction(db = database) {
                @Suppress("SqlDialectInspection")
                exec(stmt = "SELECT 1;")
                true
            }
        } catch (e: Exception) {
            tracer.warning("Database is not alive.")
            false
        }
    }

    /**
     * Builds the database location directory.
     */
    private fun buildDatabase(settings: DatabaseSettings) {
        val path: Path = Paths.get(settings.path)
        Files.createDirectories(path)
    }

    /**
     * Closes the database connection.
     * Primarily used for testing purposes.
     */
    fun close() {
        hikariDataSource?.close()
    }

    /**
     * Retrieves HikariCP health metrics.
     */
    @OptIn(HealthCheckAPI::class)
    fun getHealthCheck(): DatabaseCheck {
        return DatabaseCheck(
            alive = ping(),
            connectionTest = DatabaseCheck.ConnectionTest.build(database = database),
            datasource = DatabaseCheck.Datasource.build(datasource = hikariDataSource),
            tables = dumpTables()
        )
    }

    /**
     * Returns a list of all tables in the database.
     */
    private fun dumpTables(): List<String> {
        try {
            return transaction(db = database) {
                currentDialect.allTablesNames()
            }
        } catch (e: Exception) {
            tracer.warning("Failed to dump tables.")
            return emptyList()
        }
    }

    class SchemaBuilder {
        private val tables = mutableListOf<Table>()

        fun addTable(table: Table) {
            tables.add(table)
        }

        internal fun createTables() {
            if (tables.isNotEmpty()) {
                SchemaUtils.create(tables = tables.toTypedArray())
            }
        }
    }
}
