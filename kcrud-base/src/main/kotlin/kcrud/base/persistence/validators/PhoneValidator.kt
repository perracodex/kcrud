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
public object PhoneValidator : IValidator {
    private val tracer = Tracer<PhoneValidator>()

    public override fun check(value: String): Result<String> {
        return runCatching {
            val phoneUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()

            // Region code is null for international numbers.
            val numberProto: Phonenumber.PhoneNumber = phoneUtil.parse(value, null)

            if (!phoneUtil.isValidNumber(numberProto)) {
                throw ValidationException("Invalid phone number: $value")
            }

            return@runCatching Result.success(value)
        }.getOrElse { error ->
            when (error) {
                is NumberParseException -> {
                    tracer.error(message = "Error parsing phone number: $value", cause = error)
                    return@getOrElse Result.failure(ValidationException("Error parsing phone number: $value. ${error.message}"))
                }

                is IllegalArgumentException -> {
                    return@getOrElse Result.failure(error)
                }

                else -> {
                    return@getOrElse Result.failure(ValidationException("Unexpected error. ${error.message}"))
                }
            }
        }
    }
}
