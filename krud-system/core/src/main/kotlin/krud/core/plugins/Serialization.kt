/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package krud.core.plugins

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import krud.core.serializer.UuidSerializer
import kotlin.uuid.Uuid

/**
 * The [ContentNegotiation] plugin serves two primary purposes:
 *
 * 1. Negotiating media types between the client and server.
 * For this, it uses the Accept and Content-Type headers.
 *
 * 2. Serializing/deserializing the content in a specific format.
 * Ktor supports the following formats out-of-the-box: JSON, XML, CBOR, and ProtoBuf.
 *
 * #### References
 * - [Content negotiation and serialization](https://ktor.io/docs/serialization.html#0)
 * - [Kotlin serialization guide](https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/serialization-guide.md)
 */
public fun Application.configureSerialization() {

    install(plugin = ContentNegotiation) {

        // Define the behavior and characteristics for JSON serialization.
        json(
            Json {
                prettyPrint = true         // Format JSON output for easier reading.
                encodeDefaults = true      // Serialize properties with default values.
                ignoreUnknownKeys = false  // Fail on unknown keys in the incoming JSON.
            }
        )
    }
}

/**
 * Type alias for [Uuid] to be used in serialization.
 */
public typealias Uuid = @Serializable(with = UuidSerializer::class) Uuid
