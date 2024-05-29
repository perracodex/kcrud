/*
 * Copyright (c) 2023-Present Perracodex. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.server

import io.ktor.server.application.*
import io.ktor.server.netty.*
import kcrud.access.plugins.*
import kcrud.base.plugins.*
import kcrud.base.settings.AppSettings
import kcrud.server.plugins.configureKoin
import kcrud.server.plugins.configureRoutes
import kcrud.server.utils.ApplicationsUtils

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
    ApplicationsUtils.loadEnvironmentVariables()
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

    ApplicationsUtils.watchServer(environment = this.environment)
}
