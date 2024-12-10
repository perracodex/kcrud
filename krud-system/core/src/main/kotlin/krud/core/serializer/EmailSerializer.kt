/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package krud.core.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import krud.core.error.validator.EmailValidator

/**
 * Serializer for Email strings.
 */
internal object EmailSerializer : KSerializer<String> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        serialName = "EmailString",
        kind = PrimitiveKind.STRING
    )

    override fun serialize(encoder: Encoder, value: String) {
        EmailValidator.check(value = value).fold(
            onSuccess = { encoder.encodeString(value = value) },
            onFailure = { error -> throw SerializationException(error.message) }
        )
    }

    override fun deserialize(decoder: Decoder): String {
        val string: String = decoder.decodeString()
        EmailValidator.check(value = string).fold(
            onSuccess = { return string },
            onFailure = { error -> throw SerializationException(error.message) }
        )
    }
}

/**
 * Represents a serializable Email String.
 *
 * @property EmailString The type representing the serializable Email.
 *
 * @see [EmailSerializer]
 */
public typealias EmailString = @Serializable(with = EmailSerializer::class) String
