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
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

/**
 * Serializer for [OffsetDateTime] objects.
 */
internal object OffsetTimestampSerializer : KSerializer<OffsetDateTime> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        serialName = "OffsetTimestamp",
        kind = PrimitiveKind.STRING
    )

    private val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

    override fun serialize(encoder: Encoder, value: OffsetDateTime) {
        encoder.encodeString(value.format(formatter))
    }

    override fun deserialize(decoder: Decoder): OffsetDateTime {
        return OffsetDateTime.parse(decoder.decodeString(), formatter)
    }
}

/**
 * Represents a serializable [OffsetDateTime].
 *
 * @property OffsetDateTime The type representing the serializable [OffsetDateTime].
 * @see OffsetDateTime
 * @see OffsetTimestampSerializer
 */
public typealias OffsetTimestamp = @Serializable(with = OffsetTimestampSerializer::class) OffsetDateTime
