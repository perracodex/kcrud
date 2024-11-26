/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

@file:Suppress("ExposedReference")

import kcrud.core.test.TestUtils
import kcrud.database.test.DatabaseTestUtils
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SchemaChangeTest {

    @BeforeTest
    fun setUp() {
        TestUtils.loadSettings()
        DatabaseTestUtils.setupDatabase()
    }

    @AfterTest
    fun tearDown() {
        DatabaseTestUtils.closeDatabase()
        TestUtils.tearDown()
    }

    /**
     * Test class to verify schema change operations.
     *
     * The test flow includes the following steps:
     *
     * 1. Create a list of tenant schemas (tenant1, tenant2, tenant3) and a shared public schema.
     * 2. Set the current schema to any random schema to test that no matter the current schema, the public schema is correctly set.
     * 3. Insert a record into the PublicTable and retrieve the inserted ID.
     * 4. Verify that the PublicTable contains the expected number of records.
     * 5. Set the current schema and verify that the SchemaTable is empty, even though a matching table exists in other schemas.
     * 6. Insert a record into the SchemaTable in the current schema.
     * 7. Perform a join query between PublicTable and SchemaTable and verify that only the data from the current schema
     *  and the PublicTable are returned.
     */
    @Test
    fun testSchemaChangeOperations() {
        val schemas: List<String> = listOf("tenant1", "tenant2", "tenant3")

        transaction {
            SchemaUtils.create(PublicTable)
            schemas.forEach { schema ->
                SchemaUtils.createSchema(Schema(name = schema))
                SchemaUtils.setSchema(Schema(name = schema))
                SchemaUtils.create(SchemaTable)
            }

            schemas.forEachIndexed { index, schema ->
                // Change to any random schema to test that no matter the current schema
                // the public schema is correctly set.
                SchemaUtils.setSchema(Schema(name = schemas.random()))

                // Insert data into PublicTable
                val newUserId: Int = PublicTable.insert {
                    it[name] = "Actor $index in public"
                } get PublicTable.id

                // Verify that the data was inserted into the PublicTable
                val publicRecords: Long = PublicTable.selectAll().count()
                assertEquals(expected = index + 1L, actual = publicRecords)

                // Set the current schema, and verify it has no data
                // in the SchemaTable, even though a matching table exists in other schemas.
                SchemaUtils.setSchema(Schema(name = schema))
                assertEquals(expected = 0, actual = SchemaTable.selectAll().count())

                // Insert data into SchemaTable in the current schema.
                SchemaTable.insert {
                    it[userId] = newUserId
                    it[amount] = 100.toBigDecimal() + (index + 1).toBigDecimal()
                }

                // Query to join PublicTable and SchemaTable.
                // The result should be the data from PublicTable and from
                // SchemaTable in the current schema only.
                val joinResult: List<Pair<String, Int>> = PublicTable
                    .innerJoin(SchemaTable)
                    .selectAll().map { row ->
                        row[PublicTable.name] to row[SchemaTable.amount].toInt()
                    }

                assertEquals(expected = 1, actual = joinResult.size)
                assertEquals(expected = "Actor $index in public", actual = joinResult.first().first)
                assertEquals(expected = 100 + (index + 1), actual = joinResult.first().second)
            }
        }
    }
}

// PublicTable is defined in the public schema, note the name "public.user"
private object PublicTable : Table(name = "public.user") {
    val id = integer(name = "id").autoIncrement()
    val name = varchar(name = "name", length = 50)
    override val primaryKey = PrimaryKey(id)
}

private object SchemaTable : Table(name = "schema_table") {
    val id = integer(name = "id").autoIncrement()
    val userId = integer(name = "user_id").references(PublicTable.id)
    val amount = decimal(name = "amount", precision = 10, scale = 2)
    override val primaryKey = PrimaryKey(id)
}
