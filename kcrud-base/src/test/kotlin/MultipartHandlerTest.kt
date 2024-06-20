/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

import io.ktor.client.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kcrud.base.utils.MultipartHandler
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class MultipartHandlerTest {

    @Serializable
    private data class TestRequest(val text: String, val userId: String)

    @Test
    fun testMultipartHandler(): Unit = testApplication {
        val tempUploadPath: File = createTempDirectory().toFile()
        val fileDescription = "Test file description"
        val request = TestRequest(text = "Hello", userId = "U12345")
        val filename = "testfile.txt"

        try {
            application {
                routing {
                    post("/test-endpoint") {
                        val multipart: MultiPartData = call.receiveMultipart()

                        MultipartHandler<TestRequest>(uploadsPath = tempUploadPath.absolutePath).receive(
                            multipart,
                            TestRequest.serializer()
                        ).let { response ->

                            assertNotNull(actual = response)
                            assertNotNull(actual = response.request)
                            assertNotNull(actual = response.fileDescription)
                            assertNotNull(actual = response.file)
                            assertEquals(expected = request, actual = response.request)
                            assertEquals(expected = fileDescription, actual = response.fileDescription)
                            assertEquals(expected = tempUploadPath.absolutePath, actual = response.file!!.parentFile?.absolutePath)
                            assertEquals(expected = filename, actual = response.file!!.name)

                            call.respond(HttpStatusCode.OK, "Request processed.")
                        }
                    }
                }
            }

            val client: HttpClient = createClient {
                this@testApplication.install(ContentNegotiation) {
                    json()
                }
            }

            val response: HttpResponse = client.submitFormWithBinaryData(
                url = "/test-endpoint",
                formData = formData {
                    append(
                        key = "request",
                        value = Json.encodeToString(request)
                    )
                    append(
                        key = "file-description",
                        value = fileDescription
                    )
                    append(
                        key = "file",
                        value = byteArrayOf(1, 2, 3, 4, 5),
                        headers = Headers.build {
                            append(HttpHeaders.ContentDisposition, "form-data; name=\"file\"; filename=\"$filename\"")
                        }
                    )
                }
            )

            assertEquals(expected = HttpStatusCode.OK, actual = response.status)
            assertEquals(expected = "Request processed.", actual = response.bodyAsText())

        } finally {
            tempUploadPath.deleteRecursively()
        }
    }
}
