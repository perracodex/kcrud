/*
 * Copyright (c) 2023-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.server

import io.ktor.server.application.*
import io.ktor.server.netty.*
import kcrud.access.plugins.configureBasicAuthentication
import kcrud.access.plugins.configureJwtAuthentication
import kcrud.access.plugins.configureRbac
import kcrud.access.plugins.configureSessions
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
 * See: [Choosing an engine](https://ktor.io/docs/engines.html)
 *
 * See: [Configure an engine](https://ktor.io/docs/engines.html#configure-engine)
 *
 * See: [Application Monitoring](https://ktor.io/docs/events.html)
 *
 * @param args Command line arguments passed to the application.
 */
fun main(args: Array<String>) {
    EngineMain.main(args)
}

/**
 * Application configuration module, responsible for setting up the server with various plugins.
 * Execution order is important as some configurations depend on the prior initialization of others.
 *
 * See: [Modules](https://ktor.io/docs/modules.html)
 *
 * See: [Plugins](https://ktor.io/docs/plugins.html#install)
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

    configureRateLimit()

    configureRbac()

    configureBasicAuthentication()

    configureJwtAuthentication()

    configureSessions()

    configureRoutes()

    configuredDocumentation()

    configureMicroMeterMetrics()

    configureStatusPages()

    configureDoubleReceive()

    configureJobScheduler()

    dumpEndpoints(environment = this.environment)

    val tracer = Tracer.byFunction(ref = ::kcrudModule)
    tracer.withSeverity("Development Mode Enabled: ${environment.developmentMode}.")
    tracer.info("Server configured. Environment: ${AppSettings.runtime.environment}.")
}

private fun dumpEndpoints(environment: ApplicationEnvironment) {
    environment.monitor.subscribe(definition = ServerReady) {
        NetworkUtils.logEndpoints(reason = "Demo", endpoints = listOf("demo?page=0&size=24"))
        NetworkUtils.logEndpoints(reason = "Healthcheck", endpoints = listOf("health"))
        NetworkUtils.logEndpoints(reason = "Snowflake", endpoints = listOf("snowflake/${SnowflakeFactory.nextId()}"))
        NetworkUtils.logEndpoints(reason = "RBAC", endpoints = listOf("rbac/login"))
        NetworkUtils.logEndpoints(reason = "Scheduled Jobs", endpoints = listOf("scheduler"))
        NetworkUtils.logEndpoints(reason = "Micrometer Metrics", endpoints = listOf("metrics"))
        NetworkUtils.logEndpoints(
            reason = "Redoc - Swagger-UI - OpenApi",
            endpoints = listOf(
                "v1/docs${AppSettings.docs.redocPath}",
                AppSettings.docs.swaggerPath,
                AppSettings.docs.openApiPath
            )
        )
    }
}
