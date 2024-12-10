/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package krud.core.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.uuid.Uuid

/**
 * Serializer for [Uuid] objects.
 */
internal object UuidSerializer : KSerializer<Uuid> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        serialName = "kotlin_uuid",
        kind = PrimitiveKind.STRING
    )

    override fun serialize(encoder: Encoder, value: Uuid) {
        encoder.encodeString(value = value.toString())
    }

    override fun deserialize(decoder: Decoder): Uuid {
        return Uuid.parse(uuidString = decoder.decodeString())
    }
}
