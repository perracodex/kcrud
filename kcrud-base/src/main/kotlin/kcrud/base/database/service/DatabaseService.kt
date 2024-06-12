/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.database.service

import com.zaxxer.hikari.HikariDataSource
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import kcrud.base.database.annotation.DatabaseAPI
import kcrud.base.env.Tracer
import kcrud.base.env.health.annotation.HealthCheckAPI
import kcrud.base.env.health.checks.DatabaseCheck
import kcrud.base.settings.AppSettings
import kcrud.base.settings.config.sections.DatabaseSettings
import org.flywaydb.core.Flyway
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

    /** The database instance held by the service. */
    val database: Database
        get() = _database ?: throw IllegalStateException("Database not initialized.")

    private var hikariDataSource: HikariDataSource? = null

    /**
     * Initializes the database connection based on the provided mode and database type.
     *
     * @param settings The [DatabaseSettings] to be used to configure the database.
     * @param isolationLevel The isolation level to use for the database transactions.
     * @param micrometerRegistry Optional [PrometheusMeterRegistry] instance for micro-metrics monitoring.
     * @param schemaSetup Optional lambda to setup the database schema.
     */
    fun init(
        settings: DatabaseSettings,
        isolationLevel: IsolationLevel = IsolationLevel.TRANSACTION_REPEATABLE_READ,
        micrometerRegistry: PrometheusMeterRegistry? = null,
        schemaSetup: (SchemaBuilder.() -> Unit) = {}
    ) {
        buildDatabase(settings = settings)

        // Establishes a database connection.
        // If a connection pool size is specified, a HikariCP DataSource is configured to manage the pool
        val databaseInstance: Database = if (settings.connectionPoolSize > 0) {
            val dataSource: HikariDataSource = DatabasePooling.createDataSource(
                settings = settings,
                isolationLevel = isolationLevel,
                micrometerRegistry = micrometerRegistry
            )

            hikariDataSource = dataSource
            connectDatabase(settings = settings, isolationLevel = isolationLevel, datasource = dataSource)
        } else {
            connectDatabase(settings = settings, isolationLevel = isolationLevel)
        }

        schemaSetup.let {
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
    private fun connectDatabase(
        settings: DatabaseSettings,
        isolationLevel: IsolationLevel,
        datasource: HikariDataSource? = null
    ): Database {
        val databaseConfig = DatabaseConfig {
            defaultIsolationLevel = isolationLevel.id
            defaultMaxAttempts = settings.transactionMaxAttempts
            defaultMinRetryDelay = settings.transactionMinRetryDelayMs
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
        Flyway.configure().dataSource(
            settings.jdbcUrl,
            settings.username,
            settings.password
        ).load().apply {
            info().pending().let { pending ->
                if (pending.isEmpty()) {
                    tracer.info("No migrations to apply.")
                } else {
                    val migrations: String = pending.joinToString(separator = "\n") { migration ->
                        "Version: ${migration.version}. " +
                                "Description: ${migration.description}. " +
                                "Type: ${migration.type}. " +
                                "State: ${migration.state}. " +
                                "Script: ${migration.script}"
                    }

                    tracer.info("Migrations being applied:\n$migrations")
                }
            }

            migrate()
        }
    }

    /**
     * Checks whether the database is alive.
     */
    private fun ping(): Boolean {
        return try {
            transaction(db = database) {
                @Suppress("SqlDialectInspection", "SqlNoDataSourceInspection")
                exec(stmt = "SELECT 1;")
                true
            }
        } catch (e: Exception) {
            tracer.warning("Database is not alive.")
            false
        }
    }

    /**
     * Builds the database location directory if the database is a local file.
     */
    private fun buildDatabase(settings: DatabaseSettings) {
        if (settings.path.isBlank()) {
            throw IllegalArgumentException("Database path is required.")
        }

        if (settings.isLocalFile) {
            val currentWorkingPath: Path = Paths.get("").toAbsolutePath()
            val targetPath: Path = currentWorkingPath.resolve(settings.path)
            Files.createDirectories(targetPath)
        } else {
            tracer.debug("Database is not a local file. Path: ${settings.path}")
        }
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
        val databaseTest: DatabaseCheck.ConnectionTest.Result = DatabaseCheck.ConnectionTest.build(database = database)

        val databaseCheck = DatabaseCheck(
            alive = ping(),
            connectionTest = databaseTest.output,
            datasource = DatabaseCheck.Datasource.build(datasource = hikariDataSource),
            tables = dumpTables()
        )

        databaseTest.error?.let { databaseCheck.errors.add(it) }

        return databaseCheck
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

    /**
     * Builder class for setting up the database schema.
     *
     * Setting up the schema is optional, as it can be created also by migrations.
     */
    class SchemaBuilder {
        private val tables = mutableListOf<Table>()

        /**
         * Adds a table to the schema. If the table already exists, it will be ignored.
         *
         * @param table The table to add.
         */
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
