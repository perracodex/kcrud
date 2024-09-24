/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kcrud.core.utils.TestUtils
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {

    @AfterTest
    fun tearDown() {
        TestUtils.tearDown()
    }

    @Test
    fun testRoot(): Unit = testApplication {
        client.get("/").apply {
            assertEquals(expected = HttpStatusCode.OK, actual = status)
            assertEquals(expected = "Hello World.", actual = bodyAsText(), message = "Should be: Hello World.")
        }
    }
}
