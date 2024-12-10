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

/**
 * Serializer for Non Blank String objects.
 */
internal object NonBlankStringSerializer : KSerializer<String> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        serialName = "NonBlankString",
        kind = PrimitiveKind.STRING
    )

    override fun serialize(encoder: Encoder, value: String) {
        if (value.isBlank()) {
            throw SerializationException("String cannot be blank.")
        }
        encoder.encodeString(value = value)
    }

    override fun deserialize(decoder: Decoder): String {
        val string: String = decoder.decodeString()
        if (string.isBlank()) {
            throw SerializationException("String cannot be blank.")
        }
        return string
    }
}

/**
 * Represents a serializable Non Blank String.
 *
 * @property NoBlankString The type representing the serializable String.
 *
 * @see [NonBlankStringSerializer]
 */
public typealias NoBlankString = @Serializable(with = NonBlankStringSerializer::class) String
