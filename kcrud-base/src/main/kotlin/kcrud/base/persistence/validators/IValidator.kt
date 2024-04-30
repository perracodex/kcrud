/*
 * Copyright (c) 2023-Present Perraco. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.persistence.validators

/**
 * Implementing this interface allows defining custom validation
 * logic that can be applied to various data fields, such as email
 * addresses, phone numbers, or any other data requiring a validation.
 */
interface IValidator {
    /**
     * Validates the given value.
     *
     * @param value The target value to be validated.
     * @return A [Result] object containing the validation result.
     */
    fun <T> validate(value: T): Result

    /**
     * Generates an error message for an invalid value.
     *
     * @param text The value that failed validation.
     * @return A string representing the error message.
     */
    fun message(text: String): String

    /**
     * Throws a [ValidationException] with the given message.
     *
     * @param message Contextual failure message.
     * @throws ValidationException
     */
    fun raise(message: String): Nothing {
        throw ValidationException(message = message)
    }

    /**
     * Sealed class represents a validation result.
     */
    sealed class Result {
        /**
         * Represents a successful outcome of an operation.
         */
        data object Success : Result()

        /**
         * Represents a failed outcome of an operation, including an error message.
         *
         * @property reason The message describing the reason for the failure.
         */
        data class Failure(val reason: String) : Result()
    }
}

/**
 * Exception class for validation errors.
 *
 * @param message The detailed message of the validation failure.
 */
class ValidationException(message: String) : IllegalArgumentException("Validation Failed. $message")
