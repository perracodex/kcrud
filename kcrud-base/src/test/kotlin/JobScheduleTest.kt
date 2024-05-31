/*
 * Copyright (c) 2024-Present Perracodex. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

import io.ktor.test.dispatcher.*
import kcrud.base.scheduling.entity.JobScheduleRequest
import kcrud.base.scheduling.service.JobSchedulerService
import kcrud.base.scheduling.service.JobStartAt
import kcrud.base.utils.TestUtils
import kotlinx.coroutines.delay
import org.quartz.*
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

class JobSchedulerServiceTest {

    companion object {
        private val testResults = mutableListOf<String>()
    }

    @BeforeTest
    fun setUp() {
        TestUtils.loadSettings()
        TestUtils.setupDatabase()
        JobSchedulerService.start()
    }

    @AfterTest
    fun tearDown() {
        TestUtils.tearDown()
        JobSchedulerService.stop()
    }

    @Test
    fun testEntity(): Unit = testSuspend {
        val uniqueTestKey = "uniqueTestJob_${System.nanoTime()}"

        val jobKey: JobKey = JobScheduleRequest.send(jobClass = SimpleTestJob::class.java) {
            groupName = "TestGroup"
            startAt = JobStartAt.Immediate
            parameters = mapOf("uniqueKey" to uniqueTestKey)
        }

        // Wait for enough time to allow the job to execute.
        delay(timeMillis = 3000L)

        assertTrue(actual = testResults.contains(uniqueTestKey))

        // Clean up by un-scheduling the job.
        JobSchedulerService.deleteJob(key = jobKey)
    }

    @Test
    fun testJobMisfireHandling(): Unit = testSuspend {
        val jobKey: JobKey = JobKey.jobKey("misfireTestJob", "testGroup")
        val jobDetail: JobDetail = JobBuilder.newJob(MisfireTestJob::class.java)
            .withIdentity(jobKey)
            .build()

        // Configure the trigger to misfire by using a tight schedule and a short misfire threshold.
        val trigger = TriggerBuilder.newTrigger()
            .withIdentity("misfireTestTrigger", "testGroup")
            .startAt(DateBuilder.futureDate(2, DateBuilder.IntervalUnit.SECOND)) // Start in the near future.
            .withSchedule(
                SimpleScheduleBuilder.simpleSchedule()
                    .withMisfireHandlingInstructionFireNow()
            ) // Set misfire instruction.
            .build()

        JobSchedulerService.newJob(job = jobDetail, trigger = trigger)

        // Wait enough time to ensure the job has time to misfire plus some buffer.
        delay(timeMillis = 4000L)

        // Verify the job misfire has been handled.
        assertTrue(actual = testResults.contains(MisfireTestJob.MISFIRE_MESSAGE))

        // Clean up
        JobSchedulerService.deleteJob(jobKey)
    }

    class MisfireTestJob : Job {
        companion object {
            const val MISFIRE_MESSAGE: String = "Misfire_Handled"
            private const val REGULAR_EXECUTION_MESSAGE: String = "Regular_Execution"
        }

        override fun execute(context: JobExecutionContext?) {
            // Check if this execution is a misfire.
            if (context?.trigger?.nextFireTime == null) {
                // This means the job has been fired due to misfire handling.
                println("TEST PASSED: Misfire handled for job: ${context!!.jobDetail.key}")
                testResults.add(MISFIRE_MESSAGE)
            } else {
                // Regular execution.
                println("TEST FAILED: Expected misfire handling but got regular execution for job: ${context.jobDetail.key}")
                testResults.add(REGULAR_EXECUTION_MESSAGE)
            }
        }
    }

    class SimpleTestJob : Job {
        override fun execute(context: JobExecutionContext?) {
            val uniqueKey = context?.mergedJobDataMap?.getString("uniqueKey") ?: "defaultKey"
            testResults.add(uniqueKey)
            println("Test job executed with unique key: $uniqueKey")
        }
    }
}
