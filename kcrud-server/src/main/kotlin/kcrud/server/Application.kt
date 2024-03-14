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
import kcrud.base.infrastructure.utils.Tracer
import kcrud.base.plugins.*
import kcrud.base.settings.AppSettings
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

    val tracer = Tracer.byFunction(ref = ::kcrudModule)
    tracer.byEnvironment("Development Mode Enabled: ${environment.developmentMode}.")
    tracer.info("Server configured. Environment: ${AppSettings.runtime.environment}.")
}
