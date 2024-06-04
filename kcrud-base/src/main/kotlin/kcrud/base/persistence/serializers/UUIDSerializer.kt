/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.persistence.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.*

/**
 * Serializer for UUID objects.
 */
internal object UUIDSerializer : KSerializer<UUID> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        serialName = "UUID",
        kind = PrimitiveKind.STRING
    )

    override fun serialize(encoder: Encoder, value: UUID): Unit = encoder.encodeString(value = value.toString())

    override fun deserialize(decoder: Decoder): UUID = UUID.fromString(decoder.decodeString())
}

/**
 * Represents a serializable UUID.
 *
 * @property SUUID The type representing the serializable UUID.
 * @see UUID
 * @see UUIDSerializer
 */
typealias SUUID = @Serializable(with = UUIDSerializer::class) UUID
