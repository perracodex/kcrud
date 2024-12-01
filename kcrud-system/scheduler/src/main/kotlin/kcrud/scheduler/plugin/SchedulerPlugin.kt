/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.scheduler.plugin

import io.ktor.server.application.*
import kcrud.scheduler.service.SchedulerService

/**
 * Custom Ktor plugin to configure the task scheduler.
 */
internal val SchedulerPlugin: ApplicationPlugin<SchedulerPluginConfig> = createApplicationPlugin(
    name = "SchedulerPlugin",
    ::SchedulerPluginConfig
) {
    SchedulerService.configure(application = application)

    if (pluginConfig.autoStart) {
        SchedulerService.start()
    }
}

/**
 * Configuration for the task scheduler plugin.
 *
 * @property autoStart Whether the scheduler should start automatically after configuration.
 */
internal class SchedulerPluginConfig {
    var autoStart: Boolean = false
}
