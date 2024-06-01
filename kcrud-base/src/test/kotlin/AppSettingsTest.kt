/*
 * Copyright (c) 2024-Present Perracodex. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

import kcrud.base.settings.AppSettings
import kcrud.base.utils.TestUtils
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AppSettingsTest {

    @BeforeTest
    fun setUp() {
        TestUtils.loadSettings()
    }

    @AfterTest
    fun tearDown() {
        TestUtils.tearDown()
    }

    @Test
    fun testSerializationDeserialization() {
        // Serialize the current configuration of AppSettings.
        val serializedSettings: String = AppSettings.serialize()

        // Deserialize the JSON string back into AppSettings, updating its state.
        AppSettings.deserialize(jsonString = serializedSettings)

        // Serialize AppSettings again to compare with the original JSON string.
        val newSerializedSettings: String = AppSettings.serialize()

        // Assert that the configurations before and after deserialization are identical,
        // which confirms the integrity and consistency of the serialization process.
        assertEquals(
            expected = serializedSettings,
            actual = newSerializedSettings,
            message = "The configurations before and after deserialization should be identical."
        )
    }
}
