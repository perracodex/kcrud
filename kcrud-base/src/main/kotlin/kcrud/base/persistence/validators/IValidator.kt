/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.persistence.validators

/**
 * Interface representing a generic value validator.
 *
 * Classes implementing this interface are responsible for validating
 * specific types of input values, such as email addresses, phone numbers,
 * or any other string-based data. The validation logic should ensure that
 * the input value adheres to the required format or business rules.
 *
 * @see EmailValidator
 * @see PhoneValidator
 * @see ValidationException
 */
public interface IValidator {
    /**
     * Validates the provided [value].
     *
     * @param value The target value to be validated.
     * @return A [Result] object containing original [value] if the validation is successful,
     * or a failure with a relevant [ValidationException] if the validation fails.
     */
    public fun check(value: String): Result<String>
}

/**
 * Custom exception class to represent validation errors.
 *
 * This exception should be thrown when validation of a value fails.
 * It provides a clear distinction from other types of exceptions.
 *
 * @see IValidator
 * @see EmailValidator
 * @see PhoneValidator
 *
 * @param message The detail message describing the validation failure.
 * @param cause The optional cause of the exception.
 */
public class ValidationException(message: String, cause: Throwable? = null) : Exception(message, cause)
