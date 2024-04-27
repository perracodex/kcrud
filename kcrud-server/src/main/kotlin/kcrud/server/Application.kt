/*
 * Copyright (c) 2023-Present Perraco. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.server

import io.github.cdimascio.dotenv.DotenvException
import io.github.cdimascio.dotenv.dotenv
import io.ktor.server.application.*
import io.ktor.server.netty.*
import kcrud.access.plugins.*
import kcrud.base.env.Tracer
import kcrud.base.plugins.*
import kcrud.base.security.snowflake.SnowflakeFactory
import kcrud.base.settings.AppSettings
import kcrud.base.utils.NetworkUtils
import kcrud.server.plugins.configureKoin
import kcrud.server.plugins.configureRoutes

/**
 * Application main entry point.
 * Launches the Ktor server using Netty as the application engine.
 *
 * See: [Choosing an engine](https://ktor.io/docs/server-engines.html)
 *
 * See: [Configure an engine](https://ktor.io/docs/server-engines.html#configure-engine)
 *
 * See: [Application Monitoring With Server Events](https://ktor.io/docs/server-events.html)
 *
 * @param args Command line arguments passed to the application.
 */
fun main(args: Array<String>) {
    loadEnvironmentVariables()
    EngineMain.main(args)
}

/**
 * Application configuration module, responsible for setting up the server with various plugins.
 * Execution order is important as some configurations depend on the prior initialization of others.
 *
 * See: [Modules](https://ktor.io/docs/server-modules.html)
 *
 * See: [Plugins](https://ktor.io/docs/server-plugins.html)
 */
fun Application.kcrudModule() {

    AppSettings.load(applicationConfig = environment.config)

    configureKoin()

    configureDatabase()

    configureCors()

    configureSecureConnection()

    configureHeaders()

    configureHttp()

    configureCallLogging()

    configureContentNegotiation()

    configureRateLimit()

    configureRbac()

    configureBasicAuthentication()

    configureJwtAuthentication()

    configureOAuthAuthentication()

    configureSessions()

    configureRoutes()

    configuredApiSchema()

    configureMicroMeterMetrics()

    configureStatusPages()

    configureDoubleReceive()

    configureJobScheduler()

    dumpEndpoints(environment = this.environment)

    val tracer = Tracer(ref = Application::kcrudModule)
    tracer.withSeverity("Development Mode Enabled: ${environment.developmentMode}.")
    tracer.info("Server configured. Environment: ${AppSettings.runtime.environment}.")
}

/**
 * Loads environment variables from the project `.env` file and sets them as system properties.
 * This allows the application to access these variables throughout the application
 * without having to explicitly having to create at OS level.
 */
private fun loadEnvironmentVariables() {
    val tracer = Tracer(ref = ::loadEnvironmentVariables)
    tracer.info("Loading environment variables from '.env' file.")

    try {
        val dotenv = dotenv()

        dotenv.entries().forEach { entry ->
            System.setProperty(entry.key, entry.value)
        }
    } catch (e: DotenvException) {
        tracer.info("No '.env' file found. Defaulting to system environment variables.")
    }
}

/**
 * Dumps the server's endpoints to the console for easy access and testing.
 * This does not include the actual API routes endpoints.
 */
private fun dumpEndpoints(environment: ApplicationEnvironment) {
    environment.monitor.subscribe(definition = ServerReady) {
        NetworkUtils.logEndpoints(reason = "Demo", endpoints = listOf("demo?page=0&size=24"))
        NetworkUtils.logEndpoints(reason = "Healthcheck", endpoints = listOf("health"))
        NetworkUtils.logEndpoints(reason = "Snowflake", endpoints = listOf("snowflake/${SnowflakeFactory.nextId()}"))
        NetworkUtils.logEndpoints(reason = "RBAC", endpoints = listOf("rbac/login"))
        NetworkUtils.logEndpoints(reason = "Scheduled Jobs", endpoints = listOf("scheduler"))
        NetworkUtils.logEndpoints(reason = "Micrometer Metrics", endpoints = listOf("metrics"))
        NetworkUtils.logEndpoints(
            reason = "Swagger, Redoc, OpenApi",
            endpoints = listOf(
                AppSettings.apiSchema.swaggerEndpoint,
                AppSettings.apiSchema.redocEndpoint,
                AppSettings.apiSchema.openApiEndpoint,
            )
        )
    }
}
