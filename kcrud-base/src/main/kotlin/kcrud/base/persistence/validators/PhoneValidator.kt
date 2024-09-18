/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.persistence.validators

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber
import kcrud.base.env.Tracer

/**
 * Verifies if a phone number is in the correct format.
 */
public object PhoneValidator {
    private val tracer = Tracer<PhoneValidator>()

    /**
     * Validates the given [value] as a phone number.
     *
     * @param value The phone number to be validated.
     * @return A [Result] object containing the validation result.
     */
    public fun validate(value: String): Result<Unit> {
        return runCatching {
            val phoneUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()

            // Region code is null for international numbers.
            val numberProto: Phonenumber.PhoneNumber = phoneUtil.parse(value, null)

            if (!phoneUtil.isValidNumber(numberProto)) {
                throw IllegalArgumentException("Invalid phone number. $value")
            }

            return@runCatching Result.success(Unit)
        }.getOrElse { e ->
            when (e) {
                is NumberParseException -> {
                    tracer.error(message = "Error parsing phone number: $value", cause = e)
                    return@getOrElse Result.failure(RuntimeException(e.message ?: "Error parsing phone number. $value"))
                }

                is IllegalArgumentException -> {
                    return@getOrElse Result.failure(RuntimeException(e.message ?: "Invalid phone number."))
                }

                else -> {
                    return@getOrElse Result.failure(RuntimeException("Unexpected error: ${e.message}"))
                }
            }
        }
    }
}
