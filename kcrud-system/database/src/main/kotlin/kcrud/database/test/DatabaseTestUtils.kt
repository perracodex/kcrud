/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.database.test

import kcrud.core.settings.AppSettings
import kcrud.core.util.DateTimeUtils.current
import kcrud.database.model.Period
import kcrud.database.schema.contact.ContactTable
import kcrud.database.schema.employee.EmployeeTable
import kcrud.database.schema.employment.EmploymentTable
import kcrud.database.service.DatabaseService
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

/**
 * Common utilities for unit testing.
 */
public object DatabaseTestUtils {

    /**
     * Sets up the database for testing.
     */
    public fun setupDatabase() {
        DatabaseService.init(
            settings = AppSettings.database,
            environment = AppSettings.runtime.environment
        ) {
            addTable(table = ContactTable)
            addTable(table = EmployeeTable)
            addTable(table = EmploymentTable)
        }
    }

    /**
     * Closes the database connection.
     */
    public fun closeDatabase() {
        DatabaseService.close()
    }

    /**
     * Generates a random phone number.
     */
    public fun randomPhoneNumber(): String {
        return PhoneNumberGenerator.generateUniqueUSPhoneNumber()
    }

    /**
     * Generates a random email.
     */
    public fun randomDob(): LocalDate {
        val year: Int = (1960..2000).random()
        val month: Int = (1..12).random()
        val day: Int = (1..28).random()
        return LocalDate(year = year, monthNumber = month, dayOfMonth = day)
    }

    /**
     * Generates a random [kcrud.database.model.Period].
     *
     * @param threshold The threshold date to start generating [kcrud.database.model.Period]s from.
     */
    public fun randomPeriod(threshold: LocalDate): Period {
        val startYear: Int = threshold.year + 18 + Random.nextInt(from = 0, until = 5)
        val startMonth: Int = Random.nextInt(from = 1, until = 13)
        val startDay: Int = Random.nextInt(from = 1, until = 29)
        val startDate = LocalDate(year = startYear, monthNumber = startMonth, dayOfMonth = startDay)

        // Give 80% chance for isActive to be true.
        val isActive: Boolean = Random.nextInt(from = 0, until = 100) < 80

        val endDate: LocalDate? = if (!isActive) {
            LocalDate(
                year = startYear + Random.nextInt(from = 1, until = 5),
                monthNumber = Random.nextInt(from = 1, until = 13),
                dayOfMonth = Random.nextInt(from = 1, until = 29)
            )
        } else {
            null
        }

        return Period(
            isActive = isActive,
            startDate = startDate,
            endDate = endDate,
            comments = "Randomly generated period."
        )
    }

    /**
     * Generates a random actor name.
     */
    public fun randomName(): String {
        val beginning: List<String> = listOf("Bel", "Nar", "Jen", "Mar", "Car", "Dan", "El", "San", "Chi", "Am")
        val middle: List<String> = listOf("li", "ven", "na", "la", "son", "fer", "man", "der", "tan", "ron")
        val end: List<String> = listOf("a", "o", "y", "e", "n", "d", "r", "th", "s", "m")
        return beginning.random() + middle.random() + end.random()
    }
}

/**
 * Utility class to generate random unique US test phone numbers.
 */
private object PhoneNumberGenerator {
    private val usedNumbers = ConcurrentHashMap<String, Boolean>()
    private val random: Random = Random(Instant.current().toEpochMilliseconds())
    private val areaCodes: List<Int> = listOf(212, 310, 415, 512, 607, 702)
    private val lock = Any()

    /**
     * Generates a random unique US phone number.
     *
     * @return A random unique US phone number.
     */
    fun generateUniqueUSPhoneNumber(): String {
        return synchronized(lock) {
            var phoneNumber: String
            do {
                val areaCode: Int = areaCodes.random(random) // Randomly select a valid area code
                val exchangeCode: Int = random.nextInt(200, 999) // Generate a valid exchange code
                val subscriberNumber: Int = random.nextInt(1000, 9999) // Generate a subscriber number
                phoneNumber = "+1$areaCode$exchangeCode$subscriberNumber"
            } while (usedNumbers.containsKey(phoneNumber))
            usedNumbers[phoneNumber] = true
            return@synchronized phoneNumber
        }
    }
}
