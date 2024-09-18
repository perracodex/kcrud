/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.persistence.validators

/**
 * Verifies if an email address is in the correct format.
 * It adheres to the RFC 5321 specification, which defines the standard format for email addresses.
 *
 * The top-level domain (TLD) must be at least two characters long.
 * The total length of the email address must not exceed 254 characters (as per RFC 5321).
 *
 * The local part of the email (before the '@') allows:
 * ```
 *      • Uppercase and lowercase letters (A-Z, a-z)
 *      • Digits (0-9)
 *      • Characters: dot (.), underscore (_), percent (%), plus (+), hyphen (-)
 *      • Maximum length of 64 characters (as per RFC 5321)
 * ```
 * The domain part of the email (after the '@') can include:
 * ```
 *      • Letters (A-Z, a-z)
 *      • Digits (0-9)
 *      • Hyphens (-)
 * ```
 *
 * Examples of valid email formats:
 * ```
 *      • example@email.com
 *      • user.name+tag+sorting@example.co.uk
 *      • user_name@example.org
 *      • username@example.travel
 *      • user1234@example-company.com
 * ```
 * Examples of invalid email formats:
 * ```
 *      • any-plain-text
 *      • @no-local-part.com
 *      • .email@example.com (local part starts with a dot)
 *      • email.@example.com (local part ends with a dot)
 *      • email..email@example.com (local part has consecutive dots)
 *      • email@example (no top-level domain)
 *      • email@example...com (top-level domain has consecutive dots)
 * ```
 */
public object EmailValidator {
    private const val MAX_EMAIL_LENGTH: Int = 254
    private const val MAX_LOCAL_PART_LENGTH: Int = 64
    private const val DOMAIN_SEPARATOR: String = "@"
    private val EMAIL_REGEX: Regex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,64}$".toRegex()

    /**
     * Validates the given [value] as an email address.
     *
     * @param value The email address to be validated.
     * @return A [Result] object containing the validation result.
     */
    public fun validate(value: String): Result<Unit> {
        if (!value.matches(regex = EMAIL_REGEX)) {
            return Result.failure(IllegalArgumentException("Email does not match the required format: $value"))
        }

        // Check for the maximum length of the entire email address (254 characters).
        if (value.length > MAX_EMAIL_LENGTH) {
            return Result.failure(IllegalArgumentException("Email exceeds the maximum length of 254 characters: $value"))
        }

        // Splitting local and domain parts to apply specific checks.
        val parts: List<String> = value.split(DOMAIN_SEPARATOR)
        val localPart: String = parts[0]
        val domainPart: String = parts[1]

        // Check for the maximum length of the local part (64 characters).
        if (localPart.length > MAX_LOCAL_PART_LENGTH) {
            return Result.failure(IllegalArgumentException("Email local part exceeds the maximum length of 64 characters: $value"))
        }

        // Ensure domain part does not have consecutive dots.
        if (domainPart.contains(other = "..")) {
            return Result.failure(IllegalArgumentException("Email domain part contains consecutive dots: $value"))
        }

        // Check if the local part starts or ends with a dot, or contains consecutive dots.
        if (localPart.startsWith(prefix = ".") || localPart.endsWith(suffix = ".") || localPart.contains(other = "..")) {
            return Result.failure(IllegalArgumentException("Email local part contains consecutive dots: $value"))
        }

        return Result.success(Unit)
    }
}
