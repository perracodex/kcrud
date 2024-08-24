/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.persistence.validators

/**
 * Implementing this interface allows defining custom validation
 * logic that can be applied to various data fields, such as email
 * addresses, phone numbers, or any other data requiring a validation.
 */
public interface IValidator {
    /**
     * Validates the given value.
     *
     * @param value The target value to be validated.
     * @return A [Result] object containing the validation result.
     */
    public fun <T> validate(value: T): Result

    /**
     * Generates an error message for an invalid value.
     *
     * @param text The value that failed validation.
     * @return A string representing the error message.
     */
    public fun message(text: String): String

    /**
     * Sealed class represents a validation result.
     */
    public sealed class Result {
        /**
         * Represents a successful outcome of an operation.
         */
        public data object Success : Result()

        /**
         * Represents a failed outcome of an operation, including an error message.
         *
         * @property reason The message describing the reason for the failure.
         */
        public data class Failure(val reason: String) : Result()
    }
}

/**
 * Exception class for validation errors.
 *
 * @param message The detailed message of the validation failure.
 */
public class ValidationException(message: String) : IllegalArgumentException("Validation Failed. $message")
