/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

import kcrud.base.utils.TestUtils
import org.jetbrains.exposed.sql.Schema
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SchemaTest {

    @BeforeTest
    fun setUp() {
        TestUtils.loadSettings()
        TestUtils.setupDatabase()
    }

    @AfterTest
    fun tearDown() {
        TestUtils.tearDown()
    }

    @Test
    fun testSchemaCreation() {
        val schemaA = Schema(name = "SCHEMA_A")
        val schemaB = Schema(name = "SCHEMA_B")

        transaction {
            SchemaUtils.createSchema(schemaA, schemaB)

            SchemaUtils.setSchema(schema = schemaA)
            assertEquals(expected = schemaA.identifier, actual = TransactionManager.current().connection.schema)

            SchemaUtils.setSchema(schema = schemaB)
            assertEquals(expected = schemaB.identifier, actual = TransactionManager.current().connection.schema)
        }
    }

    @Test
    fun testNestedSchema() {
        val schemaA = Schema(name = "SCHEMA_A")
        val schemaB = Schema(name = "SCHEMA_B")

        transaction {
            SchemaUtils.createSchema(schemaA, schemaB)

            // Start with schemaA.
            SchemaUtils.setSchema(schema = schemaA)
            assertEquals(expected = schemaA.identifier, actual = TransactionManager.current().connection.schema)

            transaction {
                // Change to schemaB.
                SchemaUtils.setSchema(schema = schemaB)
                assertEquals(expected = schemaB.identifier, actual = TransactionManager.current().connection.schema)
            }

            // Should be back to schemaA.
            assertEquals(expected = schemaA.identifier, actual = TransactionManager.current().connection.schema)
        }
    }
}
