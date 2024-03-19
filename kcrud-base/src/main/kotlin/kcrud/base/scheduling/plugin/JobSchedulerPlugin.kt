/*
 * Copyright (c) 2024-Present Perraco. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.scheduling.plugin

import io.ktor.server.application.*
import kcrud.base.scheduling.service.JobSchedulerService

/**
 * Configuration for the job scheduler plugin.
 */
class JobSchedulerPluginConfig {
    var autoStart: Boolean = false
}

/**
 * Custom Ktor plugin to configure the job scheduler.
 */
val JobSchedulerPlugin = createApplicationPlugin(
    name = "JobSchedulerPlugin",
    ::JobSchedulerPluginConfig
) {
    JobSchedulerService.configure(environment = application.environment)

    if (pluginConfig.autoStart) {
        JobSchedulerService.start()
    }
}
