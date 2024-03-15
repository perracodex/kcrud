/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.plugins

import io.ktor.server.application.*
import kcrud.base.scheduling.plugin.JobSchedulerPlugin
import kcrud.base.scheduling.service.JobSchedulerService

/**
 * Configures the job scheduler for chron jobs and scheduling tasks.
 *
 * Although tasks could be done via coroutines, the job scheduler is a more robust solution
 * for tasks that need to be executed at specific times or intervals, or need to ensure
 * execution even if the server is restarted.
 *
 * See: [JobSchedulerService]
 *
 * See: [Quartz Scheduler Documentation](https://github.com/quartz-scheduler/quartz/blob/main/docs/index.adoc)
 *
 * See: [Quartz Scheduler Configuration](https://www.quartz-scheduler.org/documentation/2.4.0-SNAPSHOT/configuration.html)
 */
fun Application.configureJobScheduler() {

    install(plugin = JobSchedulerPlugin) {
        autoStart = true
    }
}
