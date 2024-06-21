/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.utils

import io.ktor.http.content.*
import kcrud.base.env.Tracer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.ByteBuffer

/**
 * A generic handler for multipart form data processing.
 *
 * Sample usage:
 * ```
 * fun Route.multipartTaskRouteExample() {
 *
 *     post("some-endpoint") {
 *         val multipart: MultiPartData = call.receiveMultipart()
 *
 *         MultipartHandler <SomeRequest>().receive(
 *             multipart = multipart,
 *             serializer = SomeRequest.serializer()
 *         ).let { response ->
 *             response.request?.let { request ->
 *                 SomeService.doSomething(request = request)
 *
 *                 call.respond(
 *                     status = HttpStatusCode.OK,
 *                     message = "Some message."
 *                 )
 *             } ?: call.respond(
 *                 status = HttpStatusCode.BadRequest,
 *                 message = "Invalid request."
 *             )
 *         }
 *     }
 * }
 * ```
 *
 * @param T The type of the object expected in the request part of the form.
 * @property uploadsPath Optional path where uploaded files are stored.
 * @property persist Whether to persist the uploaded files to disk or keep them in memory.
 */
class MultipartHandler<T : Any>(
    private val uploadsPath: String = DEFAULT_UPLOADS_PATH,
    private val persist: Boolean = true
) {
    private val tracer = Tracer<MultipartHandler<T>>()

    init {
        if (persist) {
            verifyStoragePath(target = uploadsPath)
        }
    }

    /**
     * Data class to encapsulate the response from processing multipart data.
     *
     * @property request The parsed request object of type T.
     * @property files A list of [FileDetails] objects containing the file description and file.
     */
    data class MultipartResponse<T>(
        val request: T?,
        val files: List<FileDetails>
    )

    /**
     * Handles parsing multipart form data to extract a request object, a file description, and a file.
     *
     * @param multipart The MultiPartData from a client's request.
     * @param serializer The serializer for the type T, used to parse the request object.
     * @return A [MultipartResponse] object containing the parsed data.
     */
    suspend fun receive(multipart: MultiPartData, serializer: KSerializer<T>?): MultipartResponse<T> {
        var requestObject: T? = null
        val requestData = mutableMapOf<String, String>()
        val fileDetailsList = mutableListOf<FileDetails>()
        var fileDescription: String? = null

        multipart.forEachPart { part ->
            when (part) {
                is PartData.FormItem -> {
                    when (part.name) {
                        REQUEST_KEY -> {
                            requestObject = Json.decodeFromString(
                                deserializer = serializer!!,
                                string = part.value
                            )
                        }

                        FILE_DESCRIPTION_KEY -> {
                            fileDescription = part.value
                        }

                        else -> {
                            requestData[part.name!!] = part.value
                        }
                    }
                }

                is PartData.FileItem -> {
                    if (part.name!!.startsWith(FILE_KEY)) {
                        val filename = part.originalFileName ?: throw IllegalArgumentException("No filename provided.")
                        if (persist) {
                            val file = File("$uploadsPath/$filename")

                            part.streamProvider().use { input ->
                                file.outputStream().buffered().use { output ->
                                    input.copyTo(output)
                                }
                            }

                            val fileDetails = FileDetails(
                                filename = filename,
                                description = fileDescription,
                                file = file,
                            )
                            fileDetailsList.add(fileDetails)
                        } else {
                            val bytes: ByteBuffer = part.streamProvider().use { ByteBuffer.wrap(it.readBytes()) }
                            val fileDetails = FileDetails(
                                filename = filename,
                                description = fileDescription,
                                bytes = bytes
                            )
                            fileDetailsList.add(fileDetails)
                        }

                        fileDescription = null // Reset for subsequent files.
                    } else {
                        tracer.warning("Unexpected file item received: ${part.name}")
                    }
                }

                else -> {
                    tracer.warning("Unknown part type: $part")
                }
            }

            part.dispose()
        }

        // If requestObject is not set by 'request' key, try to decode from separate keys.
        if (requestObject == null && requestData.isNotEmpty()) {
            try {
                val encodedJson: String = Json.encodeToString(requestData)
                requestObject = Json.decodeFromString(
                    deserializer = serializer!!,
                    string = encodedJson
                )
            } catch (e: SerializationException) {
                throw IllegalArgumentException("Error deserializing request data: $e")
            }
        }

        return MultipartResponse(
            request = requestObject,
            files = fileDetailsList
        )
    }

    companion object {
        /** The path where uploaded files are stored. */
        private const val DEFAULT_UPLOADS_PATH = "uploads"

        /** The key for the request part in the multipart form data. */
        private const val REQUEST_KEY = "request"

        /** The key for the file description part in the multipart form data. */
        private const val FILE_DESCRIPTION_KEY = "file-description"

        /** The key for the file part in the multipart form data. */
        private const val FILE_KEY = "file"

        /**
         * Verifies the given path location exists and creates it if necessary.
         * @param target The path to verify.
         */
        fun verifyStoragePath(target: String) {
            if (target.isBlank()) {
                throw IllegalArgumentException("The uploads path cannot be empty.")
            }

            File(target).let { path ->
                if (!path.exists()) {
                    path.mkdirs()
                }
            }
        }
    }
}

/**
 * Data class to encapsulate the details of a file uploaded via multipart form data.
 *
 * @property filename The name of the file, without the path.
 * @property description The description of the file, if provided.
 * @property file The File object representing the uploaded file. Null if the file is not persisted.
 * @property bytes The ByteBuffer containing the file data. Null if the file is persisted.
 */
data class FileDetails(
    val filename: String,
    val description: String?,
    val file: File? = null,
    val bytes: ByteBuffer? = null
) {
    fun persistBytes(path: String): File {
        MultipartHandler.verifyStoragePath(target = path)

        bytes?.let { buffer ->
            val outputFile = "$path/$filename"
            File(outputFile).outputStream().buffered().use { output ->
                val byteArray = ByteArray(buffer.remaining())
                buffer.get(byteArray)
                output.write(byteArray)
            }
            return File(outputFile)
        } ?: throw IllegalArgumentException("No byte buffer to persist.")
    }
}
