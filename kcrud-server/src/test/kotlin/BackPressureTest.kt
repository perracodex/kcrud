/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.ktor.test.dispatcher.*
import io.ktor.util.collections.*
import kcrud.access.actor.entity.ActorEntity
import kcrud.access.actor.service.ActorService
import kcrud.access.actor.service.DefaultActorFactory
import kcrud.access.token.AuthenticationTokenService
import kcrud.base.database.schema.employee.types.Honorific
import kcrud.base.database.schema.employee.types.MaritalStatus
import kcrud.base.env.SessionContext
import kcrud.base.utils.TestUtils
import kcrud.domain.contact.entity.ContactRequest
import kcrud.domain.employee.entity.EmployeeRequest
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.system.measureTimeMillis
import kotlin.test.*

class BackPressureTest : KoinComponent {

    @BeforeTest
    fun setUp() {
        TestUtils.loadSettings()
        TestUtils.setupDatabase()
    }

    @AfterTest
    fun tearDown() {
        TestUtils.tearDown()
    }

    /**
     * Test the backpressure with controlled load increase
     * by sending a large number of concurrent requests to the server
     * and checking the average response time.
     */
    @Test
    fun testWithControlledLoadIncrease() = testApplication {
        startApplication()

        // Initial setup (unchanged)
        val actorService: ActorService by inject()
        val actor: ActorEntity? = actorService.findByUsername(username = DefaultActorFactory.RoleName.ADMIN.name.lowercase())
        assertNotNull(actual = actor)

        val sessionContext = SessionContext(
            actorId = actor.id,
            username = actor.username,
            roleId = actor.role.id,
            schema = null,
        )
        val authToken: String = AuthenticationTokenService.generate(sessionContext = sessionContext)

        val startConcurrency = 100
        val maxConcurrency = 1000
        val step = 100
        val operationMix: List<String> = listOf("read", "write") // Simulate a mix of read and write operations.

        var totalAverageResponseTime = 0.0
        var largestAverageResponseTime = 0.0

        val writtenEmployeeIds = ConcurrentSet<String>()

        for (currentLoad in startConcurrency..maxConcurrency step step) {
            println("Testing with concurrency level: $currentLoad")

            val totalTimeMillis = measureTimeMillis {
                testSuspend {
                    repeat(currentLoad) { index ->
                        launch {
                            val operationType: String = operationMix[index % operationMix.size]

                            if (operationType == "write") {
                                val employeeRequestJson = Json.encodeToString(
                                    serializer = EmployeeRequest.serializer(),
                                    value = createEmployeeRequest()
                                )

                                val writeResponse: HttpResponse = client.post("/v1/employees") {
                                    header(key = HttpHeaders.Authorization, value = "Bearer $authToken")
                                    contentType(type = ContentType.Application.Json)
                                    setBody(body = employeeRequestJson)
                                }

                                val writeResponseBody: String = writeResponse.bodyAsText()
                                val writeJsonElement: JsonElement = Json.parseToJsonElement(writeResponseBody)
                                val writeEmployeeId: String = writeJsonElement.jsonObject["id"]?.jsonPrimitive!!.content
                                writtenEmployeeIds.add(writeEmployeeId)

                                assertEquals(HttpStatusCode.Created, writeResponse.status)
                            } else {
                                if (writtenEmployeeIds.isEmpty()) {
                                    // If no employees are written yet then using random employeeId for read operations.
                                    // Does not matter if it does not exist.
                                    val employeeId = "9965adab-21ac-4339-8cc0-8a44c2287c95"
                                    val readResponse: HttpResponse = client.get("/v1/employees/$employeeId") {
                                        header(key = HttpHeaders.Authorization, value = "Bearer $authToken")
                                    }
                                    assertEquals(expected = HttpStatusCode.NotFound, actual = readResponse.status)
                                } else {
                                    val randomIndex = (0 until writtenEmployeeIds.size).random()
                                    val employeeId = writtenEmployeeIds.elementAt(index = randomIndex)
                                    val readResponse: HttpResponse = client.get("/v1/employees/$employeeId") {
                                        header(key = HttpHeaders.Authorization, value = "Bearer $authToken")
                                    }
                                    assertEquals(expected = HttpStatusCode.OK, actual = readResponse.status)
                                }
                            }

                            delay(timeMillis = 10L) // Simulate processing delay.
                        }
                    }
                }
            }

            val averageResponseTime = totalTimeMillis.toDouble() / currentLoad
            println("Average response time at $currentLoad concurrency: $averageResponseTime ms")

            totalAverageResponseTime += averageResponseTime
            if (averageResponseTime > largestAverageResponseTime) {
                largestAverageResponseTime = averageResponseTime
            }

            // Check if average response time is within acceptable limits.
            assertTrue(message = "Average response time should be below 500ms") { averageResponseTime < 500L }
        }

        val overallAverageResponseTime = totalAverageResponseTime / ((maxConcurrency - startConcurrency) / step + 1)
        println("Overall average response time: $overallAverageResponseTime ms")
        println("Largest average response time: $largestAverageResponseTime ms")
    }

    /**
     * Test the read backpressure operations by sending
     * a large number of concurrent read requests to the server.
     */
    @Test
    fun testConcurrentReads() = testApplication {
        // Start the application so that the Koin DI container is initialized.
        startApplication()

        // Prepare the employee request.
        val employeeRequest = createEmployeeRequest()
        val employeeRequestJson: String = Json.encodeToString(
            serializer = EmployeeRequest.serializer(),
            value = employeeRequest
        )

        // Get the admin Actor to be used for the session context and the request.
        val actorService: ActorService by inject()
        val actor: ActorEntity? = actorService.findByUsername(
            username = DefaultActorFactory.RoleName.ADMIN.name.lowercase()
        )
        assertNotNull(actual = actor)

        // Generate the session context required for the request call.
        val sessionContext = SessionContext(
            actorId = actor.id,
            username = actor.username,
            roleId = actor.role.id,
            schema = null,
        )

        // Generate the authentication token required for the request call.
        val authToken: String = AuthenticationTokenService.generate(sessionContext = sessionContext)

        // Perform the backpressure test by sending a large number
        // of concurrent requests to the server.
        val totalCalls = 10000
        testSuspend {
            val writeResponse: HttpResponse = client.post("/v1/employees") {
                header(key = HttpHeaders.Authorization, value = "Bearer $authToken")
                contentType(type = ContentType.Application.Json)
                setBody(body = employeeRequestJson)
                accept(contentType = ContentType.Application.Json)
            }

            val writeResponseBody: String = writeResponse.bodyAsText()
            val writeJsonElement: JsonElement = Json.parseToJsonElement(string = writeResponseBody)
            val writeEmployeeId: String = writeJsonElement.jsonObject["id"]?.jsonPrimitive!!.content

            val jobs: List<Deferred<Unit>> = List(size = totalCalls) {
                async {
                    val readResponse: HttpResponse = client.get("/v1/employees/$writeEmployeeId") {
                        header(key = HttpHeaders.Authorization, value = "Bearer $authToken")
                    }
                    assertEquals(expected = HttpStatusCode.OK, actual = readResponse.status)

                    val readResponseBody = readResponse.bodyAsText()
                    val readJsonElement = Json.parseToJsonElement(string = readResponseBody)
                    val readEmployeeId = readJsonElement.jsonObject["id"]?.jsonPrimitive!!.content
                    assertEquals(expected = writeEmployeeId, actual = readEmployeeId)
                }
            }

            jobs.awaitAll()
        }
    }

    /**
     * Test the write backpressure operations by sending
     * a large number of concurrent write requests to the server.
     */
    @Test
    fun testConcurrentWrites() = testApplication {
        // Start the application so that the Koin DI container is initialized.
        startApplication()

        // Prepare the employee request.
        val employeeRequest = createEmployeeRequest()
        val employeeRequestJson: String = Json.encodeToString(
            serializer = EmployeeRequest.serializer(),
            value = employeeRequest
        )

        // Get the admin Actor to be used for the session context and the request.
        val actorService: ActorService by inject()
        val actor: ActorEntity? = actorService.findByUsername(username = DefaultActorFactory.RoleName.ADMIN.name.lowercase())
        assertNotNull(actual = actor)

        // Generate the session context required for the request call.
        val sessionContext = SessionContext(
            actorId = actor.id,
            username = actor.username,
            roleId = actor.role.id,
            schema = null,
        )

        // Generate the authentication token required for the request call.
        val authToken: String = AuthenticationTokenService.generate(sessionContext = sessionContext)

        // Perform the backpressure test by sending a large number
        // of concurrent requests to the server.
        val totalCalls = 10000
        testSuspend {
            val jobs: List<Deferred<Unit>> = List(size = totalCalls) {
                async {
                    val response: HttpResponse = client.post("/v1/employees") {
                        header(key = HttpHeaders.Authorization, value = "Bearer $authToken")
                        contentType(type = ContentType.Application.Json)
                        setBody(body = employeeRequestJson)
                    }
                    assertEquals(expected = HttpStatusCode.Created, actual = response.status)
                }
            }

            jobs.awaitAll()
        }
    }

    /**
     * Test the concurrent read and write operations by sending
     * a large number of concurrent read and write requests to the server.
     */
    @Test
    fun testConcurrentReadWrite() = testApplication {
        // Start the application so that the Koin DI container is initialized.
        startApplication()

        // Get the admin Actor to be used for the session context and the request.
        val actorService: ActorService by inject()
        val actor: ActorEntity? = actorService.findByUsername(username = DefaultActorFactory.RoleName.ADMIN.name.lowercase())
        assertNotNull(actual = actor)

        // Generate the session context and authentication token.
        val sessionContext = SessionContext(
            actorId = actor.id,
            username = actor.username,
            roleId = actor.role.id,
            schema = null
        )
        val authToken: String = AuthenticationTokenService.generate(sessionContext = sessionContext)

        // Concurrently send write and read requests.
        val totalCalls = 10000
        val writtenEmployeeIds = ConcurrentSet<String>()

        testSuspend {
            val jobs: List<Deferred<Unit>> = List(size = totalCalls) { index ->

                // Alternate between write and read to simulate concurrent operations.
                if (index % 2 == 0) {
                    async {
                        // Prepare a unique employee request for write operations.
                        val employeeRequest = createEmployeeRequest()
                        val employeeRequestJson = Json.encodeToString(
                            serializer = EmployeeRequest.serializer(),
                            value = employeeRequest
                        )
                        val writeResponse: HttpResponse = client.post("/v1/employees") {
                            header(key = HttpHeaders.Authorization, value = "Bearer $authToken")
                            contentType(type = ContentType.Application.Json)
                            setBody(body = employeeRequestJson)
                        }

                        val writeResponseBody: String = writeResponse.bodyAsText()
                        val writeJsonElement: JsonElement = Json.parseToJsonElement(string = writeResponseBody)
                        val writeEmployeeId: String = writeJsonElement.jsonObject["id"]?.jsonPrimitive!!.content
                        writtenEmployeeIds.add(writeEmployeeId)

                        assertEquals(expected = HttpStatusCode.Created, actual = writeResponse.status)
                    }
                } else {
                    if (writtenEmployeeIds.isEmpty())
                        delay(timeMillis = 10L)

                    async {
                        if (writtenEmployeeIds.isEmpty()) {
                            // If no employees are written yet then using random employeeId for read operations.
                            // Does not matter if it does not exist.
                            val employeeId = "9965adab-21ac-4339-8cc0-8a44c2287c95"
                            val readResponse: HttpResponse = client.get("/v1/employees/$employeeId") {
                                header(key = HttpHeaders.Authorization, value = "Bearer $authToken")
                            }
                            assertEquals(expected = HttpStatusCode.NotFound, actual = readResponse.status)
                        } else {
                            val randomIndex = (0 until writtenEmployeeIds.size).random()
                            val employeeId = writtenEmployeeIds.elementAt(index = randomIndex)
                            val readResponse: HttpResponse = client.get("/v1/employees/$employeeId") {
                                header(key = HttpHeaders.Authorization, value = "Bearer $authToken")
                            }
                            assertEquals(expected = HttpStatusCode.OK, actual = readResponse.status)
                        }
                    }
                }
            }

            // Await completion of all jobs.
            jobs.awaitAll()
        }
    }

    private fun createEmployeeRequest(): EmployeeRequest {
        val firstName = TestUtils.randomName()
        val lastName = TestUtils.randomName()
        return EmployeeRequest(
            firstName = firstName,
            lastName = lastName,
            dob = TestUtils.randomDob(),
            honorific = Honorific.entries.random(),
            maritalStatus = MaritalStatus.entries.random(),
            contact = ContactRequest(
                email = "$lastName.$firstName@email.com",
                phone = TestUtils.randomPhoneNumber()
            )
        )
    }
}
