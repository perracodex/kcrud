/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.persistence.validators.impl

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber
import kcrud.base.env.Tracer
import kcrud.base.persistence.validators.IValidator

/**
 * Verifies if a phone number is in the correct format.
 */
public object PhoneValidator : IValidator {
    private val tracer = Tracer<PhoneValidator>()

    override fun <T> validate(value: T): IValidator.Result {
        if (value !is String) {
            return IValidator.Result.Failure(reason = "Phone number must be a string.")
        }

        return runCatching {
            val phoneUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()

            // Region code is null for international numbers.
            val numberProto: Phonenumber.PhoneNumber = phoneUtil.parse(value, null)

            if (!phoneUtil.isValidNumber(numberProto)) {
                throw IllegalArgumentException("Invalid phone number. $value")
            }

            return@runCatching IValidator.Result.Success
        }.getOrElse { e ->
            when (e) {
                is NumberParseException -> {
                    tracer.error(message = "Error parsing phone number: $value", cause = e)
                    return@getOrElse IValidator.Result.Failure(reason = e.message ?: "Error parsing phone number. $value")
                }

                is IllegalArgumentException -> {
                    return@getOrElse IValidator.Result.Failure(reason = e.message ?: "Invalid phone number.")
                }

                else -> {
                    return@getOrElse IValidator.Result.Failure(reason = "Unexpected error: ${e.message}")
                }
            }
        }
    }

    override fun message(text: String): String {
        return "Invalid phone number: $text"
    }
}
