/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.core.scheduler.service

import kcrud.core.env.Tracer
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedSendChannelException

/**
 * An object that manages asynchronous actions for the scheduler using coroutines.
 *
 * This object provides a mechanism to enqueue suspending actions that are executed sequentially.
 * It maintains a [CoroutineScope] and a [Channel] of suspending actions.
 * Actions enqueued are processed one at a time in the order they are received.
 *
 * #### Attention
 * All actions enqueued before shutdown are guaranteed to be executed.
 * During shutdown, the application will wait for all pending actions to complete.
 */
public object SchedulerAsyncScope {
    private val tracer = Tracer<SchedulerAsyncScope>()

    /**
     * The coroutine scope used for launching the action processing coroutine.
     */
    private val scope: CoroutineScope = CoroutineScope(context = SupervisorJob() + Dispatchers.Default)

    /**
     * The channel that holds the suspending actions to be executed.
     *
     * This channel has an unlimited capacity, allowing it to buffer any number of actions.
     * Actions are suspending functions of type `suspend () -> Unit`.
     */
    private val taskChannel: Channel<suspend () -> Unit> = Channel(capacity = Channel.UNLIMITED)

    /**
     * A deferred that completes when the processing coroutine finishes.
     *
     * Used to await the completion of all pending actions during shutdown.
     */
    private val processingJob: Job = scope.launch {
        for (action in taskChannel) {
            try {
                action() // Executes each action sequentially.
            } catch (e: Throwable) {
                tracer.error(message = "Error executing async action.", cause = e)
            }
        }
    }

    /**
     * Enqueues an action to be executed by the scheduler coroutine.
     *
     * The action is a suspending function that will be executed sequentially in the order
     * it was enqueued. If the channel is closed, this method will throw an exception.
     *
     * @param action The suspending action to enqueue.
     * @throws Exception if the action cannot be enqueued because the channel is closed.
     */
    public fun enqueue(action: suspend () -> Unit) {
        scope.launch {
            try {
                // Using `send` to suspend if the channel is full or closed,
                // ensuring actions are not discarded.
                taskChannel.send(action)
            } catch (e: ClosedSendChannelException) {
                tracer.error(message = "Async action channel is closed.", cause = e)
            }
        }
    }

    /**
     * Closes the action channel and cancels the coroutine scope.
     *
     * This should be called when the application is shutting down to ensure that
     * all resources are properly released and that no new actions can be enqueued.
     *
     * @return A [Job] that can be awaited to ensure all pending actions are completed.
     */
    internal fun close(): Job {
        // Closes the channel, no new actions can be enqueued.
        taskChannel.close()
        // Return the processing job to allow awaiting its completion.
        return processingJob
    }
}
