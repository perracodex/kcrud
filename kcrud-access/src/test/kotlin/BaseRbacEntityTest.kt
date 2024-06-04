/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

import kcrud.access.rbac.entity.base.BaseRbacEntity
import kcrud.access.rbac.service.RbacFieldAnonymization
import kcrud.base.utils.KLocalDate
import kcrud.base.utils.KLocalDateTime
import kcrud.base.utils.KLocalTime
import kotlin.test.Test
import kotlin.test.assertEquals

class BaseRbacEntityTest {

    @Test
    fun testFields() {
        data class Entity(
            val id: Int,
            val name: String
        ) : BaseRbacEntity()

        val entity = Entity(
            id = 1,
            name = "name"
        )

        // Verify no anonymization.
        assertEquals(expected = 1, actual = entity.id)
        assertEquals(expected = "name", actual = entity.name)
        assertEquals(expected = RbacFieldAnonymization.isAnonymized(entity.id), actual = false)
        assertEquals(expected = RbacFieldAnonymization.isAnonymized(entity.name), actual = false)

        // Verify single field anonymization.
        val singleFieldProcessedEntity: Entity = entity.anonymize(fields = listOf("name"))
        assertEquals(expected = 1, actual = singleFieldProcessedEntity.id)
        assertEquals(expected = RbacFieldAnonymization.isAnonymized(singleFieldProcessedEntity.id), actual = false)
        assertEquals(expected = RbacFieldAnonymization.isAnonymized(singleFieldProcessedEntity.name), actual = true)

        // Verify multiple field anonymization.
        val allFieldsProcessedEntity: Entity = entity.anonymize(fields = listOf("id", "name"))
        assertEquals(expected = RbacFieldAnonymization.isAnonymized(allFieldsProcessedEntity.id), actual = true)
        assertEquals(expected = RbacFieldAnonymization.isAnonymized(allFieldsProcessedEntity.name), actual = true)
    }

    @Test
    fun testNestedEntities() {
        data class ChildEntity(
            val id: Int,
            val name: String,
            val nested: ChildEntity? = null
        ) : BaseRbacEntity()

        data class MainEntity(
            val id: Int,
            val name: String,
            val nested: ChildEntity? = null
        ) : BaseRbacEntity()

        val entity = MainEntity(
            id = 1,
            name = "name",
            nested = ChildEntity(
                id = 2,
                name = "nested",
                nested = ChildEntity(
                    id = 3,
                    name = "nested",
                    nested = ChildEntity(
                        id = 4,
                        name = "nested"
                    )
                )
            )
        )

        val processedEntity: MainEntity = entity.anonymize(
            fields = listOf(
                "id", "name", "nested.id", "nested.id", "nested.name", "nested.nested.name"
            )
        )

        assertEquals(expected = RbacFieldAnonymization.isAnonymized(processedEntity.id), actual = true)
        assertEquals(expected = RbacFieldAnonymization.isAnonymized(processedEntity.name), actual = true)
        assertEquals(expected = RbacFieldAnonymization.isAnonymized(processedEntity.nested!!.id), actual = true)
        assertEquals(expected = RbacFieldAnonymization.isAnonymized(processedEntity.nested.name), actual = true)
        assertEquals(expected = RbacFieldAnonymization.isAnonymized(processedEntity.nested.nested!!.id), actual = false)
        assertEquals(expected = RbacFieldAnonymization.isAnonymized(processedEntity.nested.nested.name), actual = true)
        assertEquals(expected = RbacFieldAnonymization.isAnonymized(processedEntity.nested.nested.nested!!.id), actual = false)
        assertEquals(expected = RbacFieldAnonymization.isAnonymized(processedEntity.nested.nested.nested.name), actual = false)
    }

    @Test
    fun testDates() {
        data class Entity(
            val id: Int,
            val date: KLocalDate,
            val time: KLocalTime,
            val dateTime: KLocalDateTime
        ) : BaseRbacEntity()

        val entity = Entity(
            id = 1,
            date = KLocalDate(year = 2021, monthNumber = 1, dayOfMonth = 1),
            time = KLocalTime(hour = 12, minute = 0, second = 0, nanosecond = 0),
            dateTime = KLocalDateTime(
                date = KLocalDate(year = 2021, monthNumber = 1, dayOfMonth = 1),
                time = KLocalTime(hour = 12, minute = 0, second = 0, nanosecond = 0)
            )
        )

        // Verify no anonymization.
        assertEquals(expected = 1, actual = entity.id)
        assertEquals(expected = RbacFieldAnonymization.isAnonymized(entity.id), actual = false)
        assertEquals(expected = RbacFieldAnonymization.isAnonymized(entity.date), actual = false)
        assertEquals(expected = RbacFieldAnonymization.isAnonymized(entity.time), actual = false)
        assertEquals(expected = RbacFieldAnonymization.isAnonymized(entity.dateTime), actual = false)

        // Verify anonymization.
        val anonymized: Entity = entity.anonymize(fields = listOf("date", "time", "dateTime"))
        assertEquals(expected = 1, actual = anonymized.id)
        assertEquals(expected = RbacFieldAnonymization.isAnonymized(anonymized.id), actual = false)
        assertEquals(expected = RbacFieldAnonymization.isAnonymized(anonymized.date), actual = true)
        assertEquals(expected = RbacFieldAnonymization.isAnonymized(anonymized.time), actual = true)
        assertEquals(expected = RbacFieldAnonymization.isAnonymized(anonymized.dateTime), actual = true)
    }

    @Test
    fun testNumbers() {
        data class Entity(
            val id: Int,
            val double: Double,
            val float: Float,
            val long: Long,
            val int: Int
        ) : BaseRbacEntity()

        val entity = Entity(
            id = 1,
            double = 1.0,
            float = 1.0f,
            long = 1L,
            int = 1
        )

        // Verify no anonymization.
        assertEquals(expected = 1, actual = entity.id)
        assertEquals(expected = RbacFieldAnonymization.isAnonymized(entity.id), actual = false)
        assertEquals(expected = RbacFieldAnonymization.isAnonymized(entity.double), actual = false)
        assertEquals(expected = RbacFieldAnonymization.isAnonymized(entity.float), actual = false)
        assertEquals(expected = RbacFieldAnonymization.isAnonymized(entity.long), actual = false)
        assertEquals(expected = RbacFieldAnonymization.isAnonymized(entity.int), actual = false)

        // Verify anonymization.
        val anonymized: Entity = entity.anonymize(fields = listOf("double", "float", "long", "int"))
        assertEquals(expected = 1, actual = anonymized.id)
        assertEquals(expected = RbacFieldAnonymization.isAnonymized(anonymized.id), actual = false)
        assertEquals(expected = RbacFieldAnonymization.isAnonymized(anonymized.double), actual = true)
        assertEquals(expected = RbacFieldAnonymization.isAnonymized(anonymized.float), actual = true)
        assertEquals(expected = RbacFieldAnonymization.isAnonymized(anonymized.long), actual = true)
        assertEquals(expected = RbacFieldAnonymization.isAnonymized(anonymized.int), actual = true)
    }
}
