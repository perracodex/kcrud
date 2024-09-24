/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.core.errors

/**
 * Provides utility functions to assist with error handling.
 */
internal object ErrorUtils {
    /**
     * Builds a detailed error message by extracting unique messages from the chain of causes
     * of the provided [cause], up to predefined number of initial error points that are most
     * relevant for diagnostics.
     *
     * @param cause The initial [Throwable] from which to start extracting the messages.
     * @return A detailed error message string, comprised of unique messages, up to the predefined limit.
     */
    fun summarizeCause(cause: Throwable): String {
        // Use a Set to keep track of unique messages.
        val uniqueMessages: LinkedHashSet<String> = linkedSetOf()
        // Maximum number of unique messages to collect.
        val maxMessages = 2

        // Iterate through the exception chain and collect unique messages until we reach the limit.
        generateSequence(cause) { it.cause }.forEach { currentCause ->
            // Add message if it is unique and we haven't reached the collecting limit.
            if (uniqueMessages.size < maxMessages) {
                currentCause.message?.let { message ->
                    if (!uniqueMessages.contains(message)) {
                        uniqueMessages.add(message)
                    }
                }
            }
        }

        // Join the collected messages with "Caused by:" if more than one, otherwise return the single message.
        return uniqueMessages.joinToString(separator = " Caused by: ")
    }
}
