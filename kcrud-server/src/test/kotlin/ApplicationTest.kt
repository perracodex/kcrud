/*
 * Copyright (c) 2024-Present Perraco. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kcrud.base.utils.TestUtils
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {

    @AfterTest
    fun tearDown() {
        TestUtils.tearDown()
    }

    @Test
    fun testRoot() = testApplication {
        client.get("/").apply {
            assertEquals(expected = HttpStatusCode.OK, actual = status)
            assertEquals(expected = "Hello World.", actual = bodyAsText(), message = "Should be: Hello World.")
        }
    }
}
