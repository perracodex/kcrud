/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.scheduler.plugin

import io.ktor.server.application.*
import kcrud.base.scheduler.service.core.SchedulerService

/**
 * Configuration for the task scheduler plugin.
 *
 * @property autoStart Whether the scheduler should start automatically after configuration.
 */
class SchedulerPluginConfig {
    var autoStart: Boolean = false
}

/**
 * Custom Ktor plugin to configure the task scheduler.
 */
val SchedulerPlugin: ApplicationPlugin<SchedulerPluginConfig> = createApplicationPlugin(
    name = "SchedulerPlugin",
    ::SchedulerPluginConfig
) {
    SchedulerService.configureScheduler(environment = application.environment)

    if (pluginConfig.autoStart) {
        SchedulerService.startScheduler()
    }
}
