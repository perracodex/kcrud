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
import junit.framework.TestCase.assertTrue
import kcrud.base.utils.FileDetails
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
        val fileDescription1 = "Test file description 1"
        val fileDescription2 = "Test file description 2"
        val request = TestRequest(text = "Hello", userId = "U12345")
        val filename1 = "testfile1.txt"
        val filename2 = "testfile2.txt"

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
                            assertEquals(expected = request, actual = response.request)
                            assertTrue(response.files.size >= 2)

                            val fileDetails1: FileDetails? = response.files.find {
                                it.description == fileDescription1 && it.file?.name == filename1
                            }
                            assertNotNull(actual = fileDetails1)
                            assertEquals(expected = fileDescription1, fileDetails1.description)
                            assertNotNull(actual = fileDetails1.file)
                            assertEquals(expected = tempUploadPath.absolutePath, actual = fileDetails1.file?.parentFile?.absolutePath)

                            val fileDetails2: FileDetails? = response.files.find {
                                it.description == fileDescription2 && it.file?.name == filename2
                            }
                            assertNotNull(actual = fileDetails2)
                            assertEquals(expected = fileDescription2, fileDetails2.description)
                            assertNotNull(actual = fileDetails2.file)
                            assertEquals(expected = tempUploadPath.absolutePath, actual = fileDetails2.file?.parentFile?.absolutePath)

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
                        value = fileDescription1,
                        headers = Headers.build {
                            append(
                                HttpHeaders.ContentDisposition,
                                "form-data; name=\"file-description-1\""
                            )
                        }
                    )
                    append(
                        key = "file",
                        value = byteArrayOf(1, 2, 3),
                        headers = Headers.build {
                            append(
                                HttpHeaders.ContentDisposition,
                                "form-data; name=\"file-1\"; filename=\"$filename1\""
                            )
                        }
                    )
                    append(
                        key = "file-description",
                        value = fileDescription2,
                        headers = Headers.build {
                            append(
                                HttpHeaders.ContentDisposition,
                                "form-data; name=\"file-description-2\""
                            )
                        }
                    )
                    append(
                        key = "file",
                        value = byteArrayOf(4, 5, 6),
                        headers = Headers.build {
                            append(
                                HttpHeaders.ContentDisposition,
                                "form-data; name=\"file-2\"; filename=\"$filename2\""
                            )
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
