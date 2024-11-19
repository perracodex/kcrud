/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.scheduler.plugin

import io.ktor.server.application.*
import kcrud.scheduler.service.SchedulerService

/**
 * Configures the task scheduler for scheduling tasks.
 *
 * Although tasks could be done via coroutines, the task scheduler is a more robust solution
 * for tasks that need to be executed at specific times or intervals, or need to ensure
 * execution even if the server is restarted.
 *
 * #### References
 * - [Quartz Scheduler Documentation](https://github.com/quartz-scheduler/quartz/blob/main/docs/index.adoc)
 * - [Quartz Scheduler Configuration](https://www.quartz-scheduler.org/documentation/2.4.0-SNAPSHOT/configuration.html)
 *
 * @see [SchedulerService]
 */
public fun Application.configureTaskScheduler() {

    install(plugin = SchedulerPlugin) {
        autoStart = true
    }
}
