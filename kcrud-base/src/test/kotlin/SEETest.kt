/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

import io.ktor.test.dispatcher.*
import kcrud.base.events.SEEService
import kotlinx.coroutines.*
import java.io.StringWriter
import java.io.Writer
import kotlin.test.Test
import kotlin.test.assertEquals

class SEETest {

    @Test
    fun testPushAndWriteEvent(): Unit = testSuspend {
        // The list size must not exceed the Replay constant in the SEEService.
        // as this is the number of events that will be replayed to new subscribers.
        val messages: List<String> = List(size = 100) { "Test Event $it" }

        // Launch a coroutine to write events to the writer.
        val writer: Writer = StringWriter()
        val writeJob: Job = launch(Dispatchers.IO) {
            try {
                SEEService.write(writer = writer)
            } catch (e: Exception) {
                println("Exception in write coroutine: ${e.message}")
            }
        }

        // Push multiple events.
        messages.forEach { message ->
            SEEService.push(message = message)
        }

        // Give some time for the events to be processed and written.
        withContext(Dispatchers.IO) {
            delay(timeMillis = 2000L)
        }

        // Stop the writing coroutine.
        writeJob.cancel()

        // Verify the written content.
        val expectedOutput: String = messages.joinToString(separator = "\n\n", postfix = "\n\n") { "data: $it" }
        assertEquals(
            expected = expectedOutput,
            actual = writer.toString()
        )
    }
}
