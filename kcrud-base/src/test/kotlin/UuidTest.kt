/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

import kcrud.base.persistence.utils.toUuid
import kcrud.base.persistence.utils.toUuidOrNull
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.uuid.Uuid

class UuidTest {

    @Test
    fun testValidUUID() {
        val uuidString = "6679c48b-dfb1-475d-9de4-3570b7456b08"
        val uuid: Uuid? = uuidString.toUuidOrNull()
        assertEquals(expected = Uuid.parse(uuidString = uuidString), actual = uuid)
    }

    @Test
    fun testInvalidUUIDToNull() {
        val uuid: Uuid? = "invalid-uuid-string".toUuidOrNull()
        assertNull(actual = uuid)
    }

    @Test
    fun testInvalidUUIDException() {
        assertFailsWith<IllegalArgumentException> {
            "invalid-uuid-string".toUuid()
        }
    }

    @Test
    fun testNullUUIDToNull() {
        val nullString: String? = null
        val uuid: Uuid? = nullString.toUuidOrNull()
        assertNull(actual = uuid)
    }

    @Test
    fun testBlankUUIDToNull() {
        val uuid: Uuid? = " ".toUuidOrNull()
        assertNull(actual = uuid)
    }

    @Test
    fun testBlankUUIDException() {
        assertFailsWith<IllegalArgumentException> {
            " ".toUuid()
        }
    }
}
