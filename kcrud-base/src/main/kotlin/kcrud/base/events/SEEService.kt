/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.events

import kcrud.base.env.Tracer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.Writer

/**
 * Service for managing and broadcasting events using Server-Sent Events (SSE).
 */
public object SEEService {
    private val tracer = Tracer<SEEService>()

    /**
     * The number of events to replay when a new subscriber connects.
     * This determines how many past events are sent to new subscribers.
     */
    private const val REPLAY: Int = 100

    /**
     * The buffer capacity for the event service.
     * This is the maximum number of events that can be buffered
     * in the flow before backpressure is applied.
     */
    private const val BUFFER_CAPACITY: Int = 10_000

    private val eventFlow = MutableSharedFlow<String>(
        replay = REPLAY,
        extraBufferCapacity = BUFFER_CAPACITY
    )

    /**
     * Pushes a new event message to the event flow.
     *
     * @param message The message to push to the event flow.
     */
    public suspend fun push(message: String) {
        eventFlow.emit(message)
    }

    /**
     * Writes events from the event flow to the provided Writer.
     * It collects messages from the event flow, formats them according
     * to the SSE protocol, and writes them to the writer.
     *
     * @param writer The [Writer] to which the SSE events will be written.
     * @throws IOException If an I/O error occurs during writing.
     */
    internal suspend fun write(writer: Writer) {
        try {
            eventFlow.buffer(capacity = BUFFER_CAPACITY).collect { message ->
                writeMessage(writer = writer, message = message)
            }
        } catch (e: CancellationException) {
            tracer.info("Collection cancelled.")
        } catch (e: IOException) {
            tracer.error(message = "IOException during event flow collection.", cause = e)
            throw e
        }
    }

    /**
     * Writes a message to the provided Writer in the SSE format.
     *
     * @param writer The [Writer] to which the SSE events will be written.
     * @param message The message to write.
     * @throws IOException If an I/O error occurs during writing.
     */
    private suspend fun writeMessage(writer: Writer, message: String) {
        withContext(Dispatchers.IO) {
            try {
                writer.appendLine("data: $message")
                writer.appendLine() // SSE messages should be followed by two newlines.
                writer.flush()
            } catch (e: IOException) {
                tracer.error(message = "IOException during writing response", cause = e)
                throw e
            }
        }
    }
}
