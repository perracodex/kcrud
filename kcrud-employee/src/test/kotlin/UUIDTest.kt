/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

import kcrud.base.persistence.utils.toUUID
import kcrud.base.persistence.utils.toUUIDOrNull
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class UUIDTest {

    @Test
    fun testValidUUID() {
        val uuidString = "6679c48b-dfb1-475d-9de4-3570b7456b08"
        val uuid: UUID? = uuidString.toUUIDOrNull()
        assertEquals(expected = UUID.fromString(uuidString), actual = uuid)
    }

    @Test
    fun testInvalidUUIDToNull() {
        val uuid: UUID? = "invalid-uuid-string".toUUIDOrNull()
        assertNull(actual = uuid)
    }

    @Test
    fun testInvalidUUIDException() {
        assertFailsWith<IllegalArgumentException> {
            "invalid-uuid-string".toUUID()
        }
    }

    @Test
    fun testNullUUIDToNull() {
        val nullString: String? = null
        val uuid: UUID? = nullString.toUUIDOrNull()
        assertNull(actual = uuid)
    }

    @Test
    fun testBlankUUIDToNull() {
        val uuid: UUID? = " ".toUUIDOrNull()
        assertNull(actual = uuid)
    }

    @Test
    fun testBlankUUIDException() {
        assertFailsWith<IllegalArgumentException> {
            " ".toUUID()
        }
    }
}