/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.domain.contact.entity

import kcrud.base.persistence.serializers.EmailString
import kotlinx.serialization.Serializable

/**
 * Represents the request to create/update an employee's contact details.
 *
 * The phone and email are verified at server layer level, throwing custom errors
 * if invalid.
 * For the email field, it could alternatively be verified at serializer level
 * by using the [EmailString] type. Such approach although simpler, would not
 * allow to send a more detailed error message to the client.
 *
 * @property email The contact's email. Must be a valid email.
 * @property phone The contact's phone. Must be a valid phone number.
 */
@Serializable
public data class ContactRequest(
    val email: String,
    val phone: String
)
