/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.env

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Timer
import io.micrometer.core.instrument.config.MeterFilter
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry

/**
 * Utility class for managing Micrometer metrics.
 *
 * The PrometheusMeterRegistry is a Micrometer registry that allows to monitor an application
 * using Prometheus, a popular open-source monitoring system and time series database.
 *
 * See: [Prometheus](https://prometheus.io/)
 *
 * See: [Micrometer Prometheus](https://micrometer.io/docs/registry/prometheus)
 */
public object MetricsRegistry {
    /**
     * The [PrometheusMeterRegistry] instance used to manage metrics.
     */
    internal val registry: PrometheusMeterRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT).apply {
        config().meterFilter(
            MeterFilter.deny { id ->
                id.name == "ktor.http.server.requests" && id.getTag("route") == "/rbac"
            }
        )
    }

    /**
     * Registers a new counter metric.
     *
     * @param name The name of the counter.
     * @param description The description of the counter.
     * @return The registered counter.
     */
    public fun registerCounter(name: String, description: String): Counter =
        Counter.builder(name).description(description).register(registry)

    /**
     * Registers a new timer metric.
     *
     * @param name The name of the timer.
     * @param description The description of the timer.
     * @return The registered timer.
     */
    public fun registerTimer(name: String, description: String): Timer =
        Timer.builder(name).description(description).register(registry)

    /**
     * Returns the metrics content in Prometheus text format for the response body
     * of an endpoint designated for Prometheus to scrape.
     */
    internal fun scrape(): String = registry.scrape()
}
