/*
 * Copyright (c) 2024-Present Perracodex. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.utils

import io.ktor.server.config.*
import kcrud.base.database.schema.contact.ContactTable
import kcrud.base.database.schema.employee.EmployeeTable
import kcrud.base.database.service.DatabaseService
import kcrud.base.persistence.entity.Period
import kcrud.base.settings.AppSettings
import kotlinx.coroutines.runBlocking
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.Module
import java.io.File
import kotlin.random.Random

/**
 * Common utilities for unit testing.
 */
object TestUtils {

    /**
     * Loads the application settings for testing.
     */
    fun loadSettings() {
        val testConfig = ApplicationConfig(configPath = "config/config_application.conf")

        runBlocking {
            AppSettings.load(applicationConfig = testConfig)
        }
    }

    /**
     * Sets up the database for testing.
     */
    fun setupDatabase() {
        DatabaseService.init(settings = AppSettings.database) {
            addTable(table = ContactTable)
            addTable(table = EmployeeTable)
        }
    }

    /**
     * Sets up Koin for testing.
     *
     * @param modules The modules to load.
     */
    fun setupKoin(modules: List<Module> = emptyList()) {
        startKoin {
            modules(modules)
        }
    }

    /**
     * Tears down the testing environment.
     */
    fun tearDown() {
        stopKoin()

        DatabaseService.close()

        val tempRuntime = File(AppSettings.runtime.workingDir)
        if (tempRuntime.exists()) {
            tempRuntime.deleteRecursively()
        }
    }

    /**
     * Generates a random phone number.
     */
    fun randomPhoneNumber(): String {
        val phoneSuffix: Int = (111..999).random()
        return "+34611222$phoneSuffix"
    }

    /**
     * Generates a random email.
     */
    fun randomDob(): KLocalDate {
        val year: Int = (1960..2000).random()
        val month: Int = (1..12).random()
        val day: Int = (1..28).random()
        return KLocalDate(year = year, monthNumber = month, dayOfMonth = day)
    }

    /**
     * Generates a random [Period].
     *
     * @param threshold The threshold date to start generating [Period]s from.
     */
    fun randomPeriod(threshold: KLocalDate): Period {
        val startYear: Int = threshold.year + 18 + Random.nextInt(from = 0, until = 5)
        val startMonth: Int = Random.nextInt(from = 1, until = 13)
        val startDay: Int = Random.nextInt(from = 1, until = 29)
        val startDate: KLocalDate = KLocalDate(year = startYear, monthNumber = startMonth, dayOfMonth = startDay)

        // Give 80% chance for isActive to be true.
        val isActive: Boolean = Random.nextInt(from = 0, until = 100) < 80

        val endDate: KLocalDate? = if (!isActive) {
            KLocalDate(
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
    fun randomName(): String {
        val beginning: List<String> = listOf("Bel", "Nar", "Jen", "Mar", "Car", "Dan", "El", "San", "Chi", "Am")
        val middle: List<String> = listOf("li", "ven", "na", "la", "son", "fer", "man", "der", "tan", "ron")
        val end: List<String> = listOf("a", "o", "y", "e", "n", "d", "r", "th", "s", "m")
        return beginning.random() + middle.random() + end.random()
    }
}
