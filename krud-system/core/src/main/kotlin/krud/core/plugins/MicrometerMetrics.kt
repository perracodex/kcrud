/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package krud.core.plugins

import io.github.perracodex.kopapi.dsl.operation.api
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics
import io.micrometer.core.instrument.binder.system.FileDescriptorMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.micrometer.core.instrument.binder.system.UptimeMetrics
import krud.core.env.Telemetry
import krud.core.settings.AppSettings

/**
 * The [MicrometerMetrics] plugin enables Micrometer metrics in the Ktor server application
 * and allows to choose the required monitoring system, such as Prometheus, JMX, Elastic, and so on.
 *
 * By default, Ktor exposes metrics for monitoring HTTP requests and a set of low-level metrics for
 * monitoring the JVM. Can customize these metrics or create new ones.
 *
 * #### References
 * - [MicrometerMetrics](https://ktor.io/docs/server-metrics-micrometer.html)
 * - [Micrometer](https://micrometer.io/docs/concepts)
 */
public fun Application.configureMicroMeterMetrics() {

    install(plugin = MicrometerMetrics) {
        registry = Telemetry.registry

        meterBinders = listOf(
            ClassLoaderMetrics(),
            JvmMemoryMetrics(),
            JvmGcMetrics(),
            ProcessorMetrics(),
            JvmThreadMetrics(),
            FileDescriptorMetrics(),
            UptimeMetrics()
        )
    }

    routing {
        authenticate(AppSettings.security.basicAuth.providerName, optional = !AppSettings.security.isEnabled) {
            get("/admin/metrics") {
                call.respondText(
                    status = HttpStatusCode.OK,
                    contentType = ContentType.Text.Plain,
                    text = Telemetry.scrape(),
                )
            } api {
                tags = setOf("System")
                summary = "Metrics endpoint."
                description = "Provides metrics for monitoring the application."
                operationId = "metrics"
                response<String>(status = HttpStatusCode.OK) {
                    description = "The metrics data."
                    contentType = setOf(ContentType.Text.Plain)
                }
                basicSecurity(name = "System") {
                    description = "Access to system information."
                }
            }
        }
    }
}
