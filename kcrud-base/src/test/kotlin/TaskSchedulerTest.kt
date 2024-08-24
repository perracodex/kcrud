/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

import io.ktor.test.dispatcher.*
import kcrud.base.scheduler.service.core.SchedulerService
import kcrud.base.scheduler.service.schedule.Schedule
import kcrud.base.scheduler.service.schedule.TaskStartAt
import kcrud.base.scheduler.service.task.TaskConsumer
import kcrud.base.scheduler.service.task.TaskDispatch
import kcrud.base.scheduler.service.task.TaskKey
import kcrud.base.utils.TestUtils
import kotlinx.coroutines.delay
import org.quartz.*
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class SchedulerServiceTest {

    companion object {
        private val testResults = mutableListOf<String>()
    }

    @BeforeTest
    fun setUp() {
        TestUtils.loadSettings()
        TestUtils.setupDatabase()
        SchedulerService.start()
    }

    @AfterTest
    fun tearDown() {
        TestUtils.tearDown()
        SchedulerService.stop(interrupt = true)
    }

    @Test
    fun testImmediate(): Unit = testSuspend {
        val uniqueTestKey = "uniqueTestTask_${System.nanoTime()}"

        val taskId: Uuid = Uuid.random()
        val taskKey: TaskKey = TaskDispatch(
            taskId = taskId,
            consumerClass = SimpleTestConsumer::class.java,
            startAt = TaskStartAt.Immediate,
            parameters = mapOf("uniqueKey" to uniqueTestKey)
        ).send()

        // Wait for enough time to allow the task to execute.
        delay(timeMillis = 3000L)

        assertTrue(actual = testResults.contains(uniqueTestKey))

        // Clean up by un-scheduling the task.
        SchedulerService.tasks.delete(name = taskKey.name, group = taskKey.group)
    }

    @Test
    fun testInterval(): Unit = testSuspend {
        val uniqueTestKey = "uniqueTestTask_${System.nanoTime()}"

        val interval: Schedule.Interval = Schedule.Interval(days = 0u, hours = 0u, minutes = 0u, seconds = 1u)
        val taskId: Uuid = Uuid.random()
        val taskKey: TaskKey = TaskDispatch(
            taskId = taskId,
            consumerClass = SimpleTestConsumer::class.java,
            startAt = TaskStartAt.Immediate,
            parameters = mapOf("uniqueKey" to uniqueTestKey)
        ).send(schedule = interval)

        // Wait for enough time to allow the task to execute.
        delay(timeMillis = 3000L)

        assertTrue(actual = testResults.contains(uniqueTestKey))

        // Clean up by un-scheduling the task.
        SchedulerService.tasks.delete(name = taskKey.name, group = taskKey.group)
    }

    @Test
    fun testCron(): Unit = testSuspend {
        val uniqueTestKey = "uniqueTestTask_${System.nanoTime()}"

        val cron: Schedule.Cron = Schedule.Cron(cron = "0/1 * * * * ?")
        val taskId: Uuid = Uuid.random()
        val taskKey: TaskKey = TaskDispatch(
            taskId = taskId,
            consumerClass = SimpleTestConsumer::class.java,
            startAt = TaskStartAt.Immediate,
            parameters = mapOf("uniqueKey" to uniqueTestKey)
        ).send(schedule = cron)

        // Wait for enough time to allow the task to execute.
        delay(timeMillis = 3000L)

        assertTrue(actual = testResults.contains(uniqueTestKey))

        // Clean up by un-scheduling the task.
        SchedulerService.tasks.delete(name = taskKey.name, group = taskKey.group)
    }

    @Test
    fun testTaskMisfireHandling(): Unit = testSuspend {
        val jobKey: JobKey = JobKey.jobKey("misfireTestJob", "testGroup")
        val jobDetail: JobDetail = JobBuilder.newJob(MisfireTestTask::class.java)
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

        SchedulerService.tasks.schedule(task = jobDetail, trigger = trigger)

        // Wait enough time to ensure the task has time to misfire plus some buffer.
        delay(timeMillis = 4000L)

        // Verify the task misfire has been handled.
        assertTrue(actual = testResults.contains(MisfireTestTask.MISFIRE_MESSAGE))

        // Clean up
        SchedulerService.tasks.delete(name = jobKey.name, group = jobKey.group)
    }

    class MisfireTestTask : Job {
        companion object {
            const val MISFIRE_MESSAGE: String = "Misfire_Handled"
            private const val REGULAR_EXECUTION_MESSAGE: String = "Regular_Execution"
        }

        override fun execute(context: JobExecutionContext?) {
            // Check if this execution is a misfire.
            if (context?.trigger?.nextFireTime == null) {
                // This means the task has been fired due to misfire handling.
                println("TEST PASSED: Misfire handled for task: ${context!!.jobDetail.key}")
                testResults.add(MISFIRE_MESSAGE)
            } else {
                // Regular execution.
                println("TEST FAILED: Expected misfire handling but got regular execution for task: ${context.jobDetail.key}")
                testResults.add(REGULAR_EXECUTION_MESSAGE)
            }
        }
    }

    class SimpleTestConsumer : TaskConsumer() {
        override fun start(properties: Map<String, Any>) {
            val uniqueKey: String = (properties["uniqueKey"] ?: "defaultKey") as String
            testResults.add(uniqueKey)
            println("Test task executed with unique key: $uniqueKey")
        }
    }
}
