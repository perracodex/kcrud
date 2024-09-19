/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.errors

/**
 * Provides utility functions to assist with error handling.
 */
internal object ErrorUtils {
    /**
     * Builds a detailed error message by extracting the first two unique messages from the chain of causes
     * of the provided exception, focusing on initial error points that are most relevant for diagnostics.
     *
     * @param throwable The initial throwable from which to start extracting the messages.
     * @return A detailed error message string, comprised of the first two unique messages, if available.
     */
    fun buildMessage(throwable: Throwable): String {
        // Use a set to keep track of unique messages.
        val uniqueMessages = linkedSetOf<String>()

        // Iterate through the exception chain and collect unique messages until we have two.
        generateSequence(throwable) { it.cause }.forEach { currentCause ->
            // Add message if it is unique and we don't yet have two messages.
            if (uniqueMessages.size < 2) {
                currentCause.message?.let { message ->
                    if (!uniqueMessages.contains(message)) {
                        uniqueMessages.add(message)
                    }
                }
            }
        }

        // Join the collected messages with "Caused by:" if there are exactly two,
        // or just return the single message.
        return uniqueMessages.joinToString(separator = " Caused by: ")
    }
}
