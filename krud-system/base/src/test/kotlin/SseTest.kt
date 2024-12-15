/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

import io.ktor.test.dispatcher.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import krud.base.event.SseService
import kotlin.test.Test
import kotlin.test.assertEquals

class SseTest {

    @Test
    fun testPushAndCollectEvents(): Unit = testSuspend {
        // List of messages to push, simulating various events
        val messages: List<String> = List(size = 100) { "Test Event $it" }

        // Launch a coroutine to collect events from the flow.
        val collectedMessages: MutableList<String> = mutableListOf()
        val collectJob: Job = launch {
            SseService.eventFlow.toList(collectedMessages)
        }

        // Push multiple events.
        messages.forEach { message ->
            SseService.push(message = message)
        }

        // Give time for the events to be collected.
        delay(timeMillis = 500)

        // Cancel the collecting coroutine since all events are collected.
        collectJob.cancelAndJoin()

        // Verify the collected events match the messages that were pushed.
        assertEquals(
            expected = messages,
            actual = collectedMessages
        )
    }
}
