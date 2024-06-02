/*
 * Copyright (c) 2024-Present Perracodex. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.scheduling.service

import io.ktor.server.application.*
import kcrud.base.env.Tracer
import kcrud.base.scheduling.annotation.JobSchedulerAPI
import kcrud.base.scheduling.entity.JobScheduleEntity
import kcrud.base.scheduling.entity.JobScheduleStateChangeEntity
import kcrud.base.scheduling.listener.KcrudJobListener
import kcrud.base.scheduling.listener.KcrudTriggerListener
import kcrud.base.settings.AppSettings
import kcrud.base.utils.DateTimeUtils
import org.quartz.*
import org.quartz.Trigger.TriggerState
import org.quartz.impl.StdSchedulerFactory
import org.quartz.impl.matchers.GroupMatcher
import java.io.InputStream
import java.util.*

/**
 * Configures the job scheduler for chron jobs and scheduling tasks.
 *
 * Although tasks can be done via coroutines, the job scheduler is a more robust solution for
 * tasks that need to be executed at specific times or intervals, or need to ensure execution
 * even if the server is restarted.
 *
 * See: [Quartz Scheduler Documentation](https://github.com/quartz-scheduler/quartz/blob/main/docs/index.adoc)
 *
 * See: [Quartz Scheduler Configuration](https://www.quartz-scheduler.org/documentation/2.4.0-SNAPSHOT/configuration.html)
 */
object JobSchedulerService {
    private val tracer = Tracer<JobSchedulerService>()

    /** The key used to store the application settings in the job data map. */
    const val APP_SETTINGS_KEY: String = "APP_SETTINGS"

    private const val PROPERTIES_FILE: String = "quartz.properties"
    private val scheduler: Scheduler

    init {
        tracer.debug("Configuring the job scheduler.")

        // Load the configuration properties from the quartz.properties file.
        val schema: Properties = loadConfigurationFile()
        val dataSourceName: String = schema["org.quartz.jobStore.dataSource"].toString()

        // Set the database connection properties.
        schema["org.quartz.dataSource.$dataSourceName.driver"] = AppSettings.database.jdbcDriver
        schema["org.quartz.dataSource.$dataSourceName.URL"] = AppSettings.database.jdbcUrl
        schema["org.quartz.dataSource.$dataSourceName.user"] = AppSettings.database.username ?: ""
        schema["org.quartz.dataSource.$dataSourceName.password"] = AppSettings.database.password ?: ""
        schema["org.quartz.dataSource.$dataSourceName.maxConnections"] = AppSettings.database.connectionPoolSize

        // Create the scheduler and configure it with the properties.
        val schedulerFactory: SchedulerFactory = StdSchedulerFactory(schema)
        scheduler = schedulerFactory.scheduler
        scheduler.setJobFactory(CustomJobFactory())

        tracer.debug("Job scheduler configured.")
    }

    /**
     * Loads the configuration properties from the quartz.properties file.
     */
    private fun loadConfigurationFile(): Properties {
        val properties = Properties()
        val inputStream: InputStream? = Thread.currentThread().contextClassLoader.getResourceAsStream(PROPERTIES_FILE)
        inputStream?.use { properties.load(it) }
        return properties
    }

    /**
     * Starts the job scheduler.
     */
    fun start() {
        tracer.info("Starting job scheduler.")
        hookListeners()
        scheduler.start()
        tracer.info("Job scheduler started.")
    }

    @OptIn(JobSchedulerAPI::class)
    private fun hookListeners() {
        scheduler.listenerManager.addJobListener(KcrudJobListener())
        scheduler.listenerManager.addTriggerListener(KcrudTriggerListener())
    }

    /**
     * Configures the job scheduler to shut down when the application is stopped.
     */
    fun configure(environment: ApplicationEnvironment) {
        // Add a shutdown hook to stop the scheduler when the application is stopped.
        environment.monitor.subscribe(ApplicationStopping) {
            tracer.info("Shutting down the scheduler.")
            scheduler.shutdown()
            tracer.info("Scheduler shut down.")
        }
    }

    /**
     * Stops the job scheduler.
     */
    fun stop() {
        tracer.info("Stopping job scheduler.")
        scheduler.shutdown()
    }

    /**
     * Schedules a new job with the given trigger.
     */
    fun newJob(job: JobDetail, trigger: Trigger) {
        tracer.debug("Scheduling new job. Job: $job. Trigger: $trigger.")
        scheduler.scheduleJob(job, trigger)
    }

    /**
     * Deletes a job from the scheduler.
     *
     * @param key The key of the job to be deleted.
     * @return True if the job was deleted, false otherwise.
     */
    fun deleteJob(key: JobKey): Boolean {
        tracer.debug("Deleting job. Key: $key.")
        return scheduler.deleteJob(key)
    }

    /**
     * Deletes a job from the scheduler.
     *
     * @param name The name of the job to be deleted.
     * @param group The group of the job to be deleted.
     * @return True if the job was deleted, false otherwise.
     */
    fun deleteJob(name: String, group: String): Boolean {
        tracer.debug("Deleting job.Name: $name. Group: $group.")
        return deleteJob(JobKey.jobKey(name, group))
    }

    /**
     * Deletes all jobs from the scheduler.
     *
     * @return The number of jobs deleted.
     */
    fun deleteAll(): Int {
        tracer.debug("Deleting all jobs.")
        return scheduler.getJobKeys(GroupMatcher.anyGroup()).count { jobKey ->
            scheduler.deleteJob(jobKey)
        }
    }

    /**
     * Returns a list of all job groups currently scheduled in the job scheduler.
     */
    fun getGroups(): List<String> {
        return scheduler.jobGroupNames
    }

    /**
     * Returns a snapshot list of all actual jobs currently scheduled in the job scheduler.
     *
     * @param executing True if only actively executing jobs should be returned; false to return all jobs.
     * @return A list of [JobScheduleEntity] objects representing the scheduled jobs.
     */
    fun getJobs(executing: Boolean = false): List<JobScheduleEntity> {
        val jobList: List<JobScheduleEntity> = if (executing) {
            scheduler.currentlyExecutingJobs.map { createJobScheduleEntity(jobDetail = it.jobDetail) }
        } else {
            scheduler.getJobKeys(GroupMatcher.anyGroup()).map { jobKey ->
                createJobScheduleEntity(jobDetail = scheduler.getJobDetail(jobKey))
            }
        }

        // Sort the job list by nextFireTime.
        // Jobs without a nextFireTime will be placed at the end of the list.
        return jobList.sortedBy { it.nextFireTime }
    }

    /**
     * Helper method to create a [JobScheduleEntity] from a [JobDetail] including the next fire time.
     *
     * @param jobDetail The job detail from which to create the [JobScheduleEntity].
     * @return The constructed [JobScheduleEntity].
     */
    private fun createJobScheduleEntity(jobDetail: JobDetail): JobScheduleEntity {
        val jobKey: JobKey = jobDetail.key
        val triggers: List<Trigger> = scheduler.getTriggersOfJob(jobKey)
        val nextFireTime: Date? = triggers.mapNotNull { it.nextFireTime }.minOrNull()

        // Get the most restrictive state from the list of trigger states.
        val triggerStates: List<TriggerState> = triggers.map { scheduler.getTriggerState(it.key) }
        val mostRestrictiveState: TriggerState = when {
            triggerStates.any { it == TriggerState.PAUSED } -> TriggerState.PAUSED
            triggerStates.any { it == TriggerState.BLOCKED } -> TriggerState.BLOCKED
            triggerStates.any { it == TriggerState.ERROR } -> TriggerState.ERROR
            triggerStates.any { it == TriggerState.COMPLETE } -> TriggerState.COMPLETE
            else -> TriggerState.NORMAL  // Assuming NORMAL as default if no other states are found.
        }

        return JobScheduleEntity(
            name = jobKey.name,
            group = jobKey.group,
            className = jobDetail.jobClass.simpleName,
            nextFireTime = nextFireTime?.let { DateTimeUtils.javaDateToLocalDateTime(datetime = it) },
            state = mostRestrictiveState.name,
            isDurable = jobDetail.isDurable,
            shouldRecover = jobDetail.requestsRecovery(),
            dataMap = jobDetail.jobDataMap.toList().toString(),
        )
    }

    /**
     * Pauses all jobs currently scheduled in the job scheduler.
     *
     * @return [JobScheduleStateChangeEntity] containing details of the operation.
     */
    fun pause(): JobScheduleStateChangeEntity {
        return changeJobState(targetState = TriggerState.PAUSED) { scheduler.pauseAll() }
    }

    /**
     * Pauses a concrete job currently scheduled in the job scheduler.
     *
     * @param name The name of the job to pause.
     * @param group The group of the job to pause.
     * @return True if the job was paused, false otherwise.
     */
    fun pauseJob(name: String, group: String): Boolean {
        return changeJobState(targetState = TriggerState.PAUSED) {
            scheduler.pauseJob(JobKey.jobKey(name, group))
        }.totalAffected > 0
    }

    /**
     * Resumes all jobs currently paused in the job scheduler.
     *
     * @return [JobScheduleStateChangeEntity] containing details of the operation.
     */
    fun resume(): JobScheduleStateChangeEntity {
        return changeJobState(targetState = TriggerState.NORMAL) { scheduler.resumeAll() }
    }

    /**
     * Resumes a concrete job currently scheduled in the job scheduler.
     *
     * @param name The name of the job to resume.
     * @param group The group of the job to resume.
     * @return True if the job was resume, false otherwise.
     */
    fun resumeJob(name: String, group: String): Boolean {
        return changeJobState(targetState = TriggerState.NORMAL) {
            scheduler.resumeJob(JobKey.jobKey(name, group))
        }.totalAffected > 0
    }

    /**
     * Changes the state of all jobs (either pausing or resuming them) and returns detailed results of the change.
     *
     * @param targetState The expected state of jobs after the operation.
     * @param action The lambda function that executes the state change.
     * @return JobScheduleStateChangeEntity detailing the affected job counts.
     */
    private fun changeJobState(targetState: TriggerState, action: () -> Unit): JobScheduleStateChangeEntity {
        // Retrieve the states of all jobs before and after performing the state change action.
        val beforeStates: Map<JobKey, TriggerState> = getAllJobStates()
        action()
        val afterStates: Map<JobKey, TriggerState> = getAllJobStates()

        // Count the total number of jobs that were affected by the action.
        // A job is considered affected if it was not already in the target
        // state and has changed to the target state.
        val totalAffected: Int = afterStates.count { (key, state) ->
            state == targetState && beforeStates[key]?.let { it != state } ?: true
        }

        // Count the total number of jobs that remained in the target state both
        // before and after the action. This includes jobs that were already in the
        // target state and were unaffected by the action.
        val alreadyInState: Int = afterStates.count { (key, state) ->
            state == targetState && beforeStates[key]?.let { it == state } ?: false
        }

        return JobScheduleStateChangeEntity(
            totalAffected = totalAffected,
            alreadyInState = alreadyInState,
            totalJobs = afterStates.size
        )
    }

    /**
     * Retrieves the state of all jobs currently scheduled in the job scheduler,
     * by compiling a map where each job key is associated with its most
     * restrictive (or most significant) trigger state.
     *
     * @return A map of JobKeys to their respective TriggerStates.
     */
    private fun getAllJobStates(): Map<JobKey, TriggerState> {
        // 1. Fetch all job keys across all job groups within the scheduler.
        // 2. For each job key, retrieve all associated triggers.
        // 3. For each trigger, obtain its current state.
        // 4. From the list of trigger states, find the one with the highest priority (lowest ordinal value).
        // 5. If no triggers are found, or all are null, default to TriggerState.NONE.
        return scheduler.getJobKeys(GroupMatcher.anyGroup()).associateWith { jobKey ->
            scheduler.getTriggersOfJob(jobKey).mapNotNull { trigger ->
                scheduler.getTriggerState(trigger.key)
            }.minByOrNull { it.ordinal } ?: TriggerState.NONE
        }
    }
}
