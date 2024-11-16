/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

import io.ktor.test.dispatcher.*
import kcrud.core.scheduler.service.SchedulerService
import kcrud.core.scheduler.service.schedule.Schedule
import kcrud.core.scheduler.service.schedule.TaskStartAt
import kcrud.core.scheduler.service.task.TaskConsumer
import kcrud.core.scheduler.service.task.TaskDispatch
import kcrud.core.scheduler.service.task.TaskKey
import kcrud.core.security.snowflake.SnowflakeFactory
import kcrud.core.test.TestUtils
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
        val taskValue = "uniqueTestTask_${System.nanoTime()}"

        val groupId: Uuid = Uuid.random()
        val taskId: String = SnowflakeFactory.nextId()

        val properties: Map<String, Any> = buildProperties(
            groupId = groupId,
            taskId = taskId,
            taskValue = taskValue
        )

        val taskKey: TaskKey = TaskDispatch(
            groupId = groupId,
            taskId = taskId,
            consumerClass = SimpleTestConsumer::class.java,
            startAt = TaskStartAt.Immediate,
            parameters = properties
        ).send()

        // Wait for enough time to allow the task to execute.
        delay(timeMillis = 3000L)

        assertTrue(actual = testResults.contains(taskValue))

        // Clean up by un-scheduling the task.
        SchedulerService.tasks.delete(groupId = taskKey.group, taskId = taskKey.name)
    }

    @Test
    fun testInterval(): Unit = testSuspend {
        val taskValue = "uniqueTestTask_${System.nanoTime()}"

        val interval: Schedule.Interval = Schedule.Interval(days = 0u, hours = 0u, minutes = 0u, seconds = 1u)
        val groupId: Uuid = Uuid.random()
        val taskId: String = SnowflakeFactory.nextId()

        val properties: Map<String, Any> = buildProperties(
            groupId = groupId,
            taskId = taskId,
            taskValue = taskValue
        )

        val taskKey: TaskKey = TaskDispatch(
            groupId = groupId,
            taskId = taskId,
            consumerClass = SimpleTestConsumer::class.java,
            startAt = TaskStartAt.Immediate,
            parameters = properties
        ).send(schedule = interval)

        // Wait for enough time to allow the task to execute.
        delay(timeMillis = 3000L)

        assertTrue(actual = testResults.contains(taskValue))

        // Clean up by un-scheduling the task.
        SchedulerService.tasks.delete(groupId = taskKey.group, taskId = taskKey.name)
    }

    @Test
    fun testCron(): Unit = testSuspend {
        val taskValue = "uniqueTestTask_${System.nanoTime()}"

        val cron: Schedule.Cron = Schedule.Cron(cron = "0/1 * * * * ?")
        val groupId: Uuid = Uuid.random()
        val taskId: String = SnowflakeFactory.nextId()

        val properties: Map<String, Any> = buildProperties(
            groupId = groupId,
            taskId = taskId,
            taskValue = taskValue
        )

        val taskKey: TaskKey = TaskDispatch(
            groupId = groupId,
            taskId = taskId,
            consumerClass = SimpleTestConsumer::class.java,
            startAt = TaskStartAt.Immediate,
            parameters = properties
        ).send(schedule = cron)

        // Wait for enough time to allow the task to execute.
        delay(timeMillis = 3000L)

        assertTrue(actual = testResults.contains(taskValue))

        // Clean up by un-scheduling the task.
        SchedulerService.tasks.delete(groupId = taskKey.group, taskId = taskKey.name)
    }

    @Test
    fun testTaskMisfireHandling(): Unit = testSuspend {
        val jobKey: JobKey = JobKey.jobKey("misfireTestJob", "testGroup")
        val jobDetail: JobDetail = JobBuilder.newJob(MisfireTestTask::class.java)
            .withIdentity(jobKey)
            .build()

        // Configure the trigger to misfire by using a tight schedule and a short misfire threshold.
        val trigger: SimpleTrigger = TriggerBuilder.newTrigger()
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
        SchedulerService.tasks.delete(groupId = jobKey.group, taskId = jobKey.name)
    }

    class MisfireTestTask : Job {
        companion object {
            const val MISFIRE_MESSAGE: String = "Misfire_Handled"
            private const val REGULAR_EXECUTION_MESSAGE: String = "Regular_Execution"
        }

        override fun execute(context: JobExecutionContext) {
            // Check if this execution is a misfire.
            context.trigger?.nextFireTime?.let {
                // Regular execution.
                println("TEST FAILED: Expected misfire handling but got regular execution for task: ${context.jobDetail.key}")
                testResults.add(REGULAR_EXECUTION_MESSAGE)
            }

            // This means the task has been fired due to misfire handling.
            println("TEST PASSED: Misfire handled for task: ${context.jobDetail.key}")
            testResults.add(MISFIRE_MESSAGE)
        }
    }

    /**
     * A simple test consumer for verifying task execution.
     */
    class SimpleTestConsumer : TaskConsumer<TestPayload>() {

        /**
         * Builds the [TestPayload] from the provided properties.
         *
         * @param properties The property map containing task parameters.
         * @return The constructed [TestPayload].
         * @throws IllegalArgumentException If required properties are missing or invalid.
         */
        override fun buildPayload(properties: Map<String, Any>): TestPayload {
            val groupId: Uuid = properties["GROUP_ID"] as? Uuid
                ?: throw IllegalArgumentException("GROUP_ID is missing or invalid.")
            val taskId: String = properties["TASK_ID"] as? String
                ?: throw IllegalArgumentException("TASK_ID is missing or invalid.")
            val taskValue: String = properties["TASK_VALUE"] as? String
                ?: throw IllegalArgumentException("TASK_VALUE is missing or invalid.")

            return TestPayload(
                groupId = groupId,
                taskId = taskId,
                taskValue = taskValue
            )
        }

        /**
         * Consumes the [TestPayload] by adding the unique key to the test results.
         *
         * @param payload The [TestPayload] containing task data.
         */
        override fun consume(payload: TestPayload) {
            testResults.add(payload.taskValue)
            println("Test task executed with value: ${payload.taskValue}")
        }
    }

    /**
     * Concrete implementation of [TaskConsumer.Payload] for testing purposes.
     *
     * @property groupId The group ID of the task.
     * @property taskId The unique ID of the task.
     * @property taskType The type of the task.
     * @property taskValue A value to verify task execution.
     */
    data class TestPayload(
        override val groupId: Uuid,
        override val taskId: String,
        override val taskType: String = "test",
        val taskValue: String
    ) : TaskConsumer.Payload

    private fun buildProperties(groupId: Uuid, taskId: String, taskValue: String): Map<String, Any> {
        return mapOf(
            "GROUP_ID" to groupId,
            "TASK_ID" to taskId,
            "TASK_VALUE" to taskValue
        )
    }
}
